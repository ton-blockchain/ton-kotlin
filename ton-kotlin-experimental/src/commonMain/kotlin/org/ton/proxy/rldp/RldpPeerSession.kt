package org.ton.proxy.rldp

import io.ktor.util.collections.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import org.ton.kotlin.adnl.adnl.message.AdnlMessageCustom
import org.ton.kotlin.adnl.adnl.message.AdnlMessageQuery
import org.ton.kotlin.adnl.pk.PrivateKeyEd25519
import org.ton.kotlin.adnl.pub.PublicKeyEd25519
import org.ton.kotlin.adnl.rldp.RldpAnswer
import org.ton.kotlin.adnl.rldp.RldpMessageData
import org.ton.kotlin.adnl.rldp.RldpMessagePart
import org.ton.kotlin.adnl.rldp.RldpQuery
import org.ton.kotlin.bitstring.BitString
import org.ton.logger.Logger
import org.ton.proxy.adnl.AbstractAdnlPeerSession
import org.ton.proxy.adnl.AdnlPeerSession
import kotlin.experimental.xor
import kotlin.random.Random
import kotlin.time.Duration

interface RldpPeerSession : AdnlPeerSession {
    val rldp: Rldp
}

abstract class AbstractRldpPeerSession(
    override val rldp: Rldp,
    localKey: PrivateKeyEd25519,
    remoteKey: PublicKeyEd25519
) : RldpPeerSession, AbstractAdnlPeerSession(rldp, localKey, remoteKey) {
    private val receivers = ConcurrentMap<BitString, RldpReceiver>()
    private val logger = Logger.println(toString())

    suspend fun transfer(
        data: ByteArray,
        id: BitString = BitString(Random.nextBytes(32))
    ) = transfer(data.size.toLong(), ByteReadPacket(data), id)

    suspend fun transfer(
        totalLength: Long,
        input: Input,
        id: BitString = BitString(Random.nextBytes(32)),
    ) = coroutineScope {
        withContext(RLDP_OUTPUT_DISPATCHER) {
            val transfer = RldpOutputTransfer.of(totalLength, input, id)
            receivers[id] = transfer
            try {
                transfer.transferPackets().collect { part ->
//                println("output OUTGOING: $part")
                    super.sendMessage(part.toByteArray())
                }
            } finally {
                receivers.remove(id)
            }
        }
    }

    suspend fun <T> inputTransfer(
        id: BitString,
        read: suspend (ByteReadChannel) -> T
    ) = coroutineScope {
        val transfer = RldpInputTransfer.of(id)
        receivers[id] = transfer
        val byteChannel = ByteChannel()
        try {
            val transferAckJob = launch(RLDP_INPUT_DISPATCHER) {
                transfer.transferPackets().collect { part ->
//                    println("input OUTGOING: $part")
                    super.sendMessage(part.toByteArray())
                }
            }
            val result = read(transfer.byteChannel)
//            println("result: $id - $result")
            transferAckJob.cancel()
            result
        } finally {
//            println("remove input transfer: $id")
            receivers.remove(id)
            byteChannel.close()
        }
    }

    override suspend fun sendMessage(payload: ByteArray) {
        val rldpMessage = RldpMessageData(
            id = BitString(Random.nextBytes(32)),
            data = payload
        )
        transfer(rldpMessage.toByteArray())
    }

    override suspend fun query(payload: ByteArray, timeout: Duration, maxAnswerSize: Long): ByteArray = coroutineScope {
        val queryId = BitString(Random.nextBytes(32))
        withContext(coroutineContext + CoroutineName("rldp query - $queryId")) {
            val outputTransferId = Random.nextBytes(32)
            val inputTransferId = outputTransferId.map { it xor 0xFF.toByte() }.toByteArray()
            val rldpQuery =
                RldpQuery(queryId, maxAnswerSize, Clock.System.now() + timeout, payload)
            val rldpAnswer = async {
                inputTransfer(BitString(inputTransferId)) {
                    RldpAnswer.decodeBoxed(it.readRemaining())
                }
            }
            transfer(rldpQuery.toByteArray(), BitString(outputTransferId))
            withTimeout(timeout) {
                rldpAnswer.await()
            }.data
        }
    }

    override fun receiveAdnlCustom(message: AdnlMessageCustom) {
        val part = RldpMessagePart.decodeBoxed(message.data)
        val receiver = receivers[part.transfer_id]
//        println("INCOMING: $part")
        if (receiver != null) {
            receiver.receiveRldpMessagePart(part)
        } else {
//            sendMessage(RldpComplete(part.transfer_id, part.part).toByteArray())
//            launch {
////                println("resend complete - ${part.transfer_id} ${part.part}")
//            }
        }
    }

    override fun receiveAdnlQuery(message: AdnlMessageQuery) {
        throw IllegalStateException("RLDP does not support ADNL queries")
    }

    override fun toString(): String = "RLDP[${localKey}<->${remoteKey.toAdnlIdShort()}]@${hashCode()}"

    companion object {
        val RLDP_INPUT_DISPATCHER = newSingleThreadContext("rldp-input")
        val RLDP_OUTPUT_DISPATCHER = newSingleThreadContext("rldp-output")
    }
}

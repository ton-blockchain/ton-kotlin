package org.ton.kotlin.rldp

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.adnl.AdnlAddress
import org.ton.kotlin.adnl.AdnlIdFull
import org.ton.kotlin.adnl.AdnlLocalNode
import org.ton.kotlin.adnl.AdnlPeerPair
import org.ton.kotlin.adnl.util.Hash256Map
import org.ton.kotlin.tl.TL
import kotlin.coroutines.CoroutineContext
import kotlin.experimental.xor
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class RldpLocalNode(
    val adnlLocalNode: AdnlLocalNode
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default

    fun connection(adnlIdFull: AdnlIdFull, initialAddress: AdnlAddress): RldpConnection =
        connection(adnlLocalNode.peer(adnlIdFull, initialAddress))

    fun connection(adnlPeerPair: AdnlPeerPair) = RldpConnection(adnlPeerPair)
}

typealias RldpQueryHandler = suspend RldpConnection.(transferId: ByteString, query: RldpMessage.Query) -> Unit

class RldpConnection(
    val adnl: AdnlPeerPair,
) : CoroutineScope {
    val defaultQueryTimeout = 3.seconds

    private val transfers = Hash256Map<RldpTransfer>()
    private val _queryHandler = atomic<RldpQueryHandler>({ _, _ -> })

    override val coroutineContext: CoroutineContext = adnl.coroutineContext + SupervisorJob()

    init {
        adnl.onAdnlMessage { adnlMessage ->
            val rldpMessagePart = try {
                TL.Boxed.decodeFromByteString(Rldp2MessagePart.serializer(), adnlMessage.data)
            } catch (t: Throwable) {
                return@onAdnlMessage
            }
            handleMessagePart(rldpMessagePart)
        }
    }

    suspend fun handleMessagePart(
        messagePart: Rldp2MessagePart
    ) {
        when (messagePart) {
            is Rldp2MessagePart.Part -> {
                transfer(messagePart.transferId).handleMessagePart(messagePart)
            }

            is Rldp2MessagePart.Confirm -> {
                transfers[messagePart.transferId]?.handleMessagePart(messagePart)
            }

            is Rldp2MessagePart.Complete -> {
                transfers[messagePart.transferId]?.handleMessagePart(messagePart)
            }
        }
    }

    fun onQuery(handler: RldpQueryHandler) {
        _queryHandler.value = handler
    }

    suspend fun sendAnswer(
        transferId: ByteString,
        answer: RldpMessage.Answer
    ) {

    }

    suspend fun sendMessage(
        id: ByteString,
        data: ByteString
    ) {
        val message = RldpMessage.Custom(id, data)
        val rawMessage = TL.Boxed.encodeToByteString(RldpMessage.serializer(), message)

        transferScope {
            send(rawMessage)
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun query(
        data: ByteString,
        maxAnswerSize: Long = 1024 * 1024 * 5, // 5 MB
        timeout: Duration = defaultQueryTimeout
    ): ByteString = coroutineScope {
        val queryId = ByteString(*Random.nextBytes(32))
        transferScope {
            val result = async {
                responseTransfer {
                    val rawAnswer = receive()
                    TL.Boxed.decodeFromByteString(RldpMessage.Answer.serializer(), rawAnswer)
                }
            }
            val query = RldpMessage.Query(
                queryId = queryId,
                maxAnswerSize = maxAnswerSize,
                timeout = (Clock.System.now() + timeout).epochSeconds.toInt(),
                data = data
            )
            val rawQuery = TL.Boxed.encodeToByteString(RldpMessage.serializer(), query)
            send(rawQuery)

            result.await().data
        }
    }

    private suspend fun <T> transferScope(
        id: ByteString = randomTransferId(),
        block: suspend RldpTransfer.() -> T
    ): T {
        val transfer = transfer(id)
        return try {
            transfer.block()
        } finally {
            transfers.remove(id)
        }
    }

    private suspend fun <T> RldpTransfer.responseTransfer(
        block: suspend RldpTransfer.() -> T
    ): T {
        val responseId = id.toByteArray()
        for (i in 0 until 32) {
            responseId[i] = responseId[i].xor(0xFF.toByte())
        }
        return transferScope(ByteString(*responseId), block)
    }

    private fun transfer(
        id: ByteString
    ): RldpTransfer {
        return transfers.getOrPut(id) {
            RldpTransfer(id, this)
        }
    }

    private fun randomTransferId(): ByteString {
        return ByteString(*Random.nextBytes(32))
    }
}

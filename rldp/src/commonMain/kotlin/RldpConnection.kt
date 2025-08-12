package org.ton.kotlin.rldp

import io.ktor.util.logging.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.adnl.AdnlLocalNode
import org.ton.kotlin.adnl.AdnlNode
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

    fun connection(adnlNode: AdnlNode): RldpConnection =
        connection(adnlLocalNode.peer(adnlNode))

    fun connection(adnlPeerPair: AdnlPeerPair) = RldpConnection(adnlPeerPair)
}

typealias RldpQueryHandler = suspend RldpConnection.(transferId: ByteString, query: RldpMessage.Query) -> Unit

private val LOGGER = KtorSimpleLogger("org.ton.kotlin.rldp.RldpConnection")

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

    fun handleMessagePart(
        messagePart: Rldp2MessagePart
    ) {
//        LOGGER.trace { "${messagePart.transferId.debugString()} received message: $messagePart" }
        when (messagePart) {
            is Rldp2MessagePart.Part -> {
                transfers[messagePart.transferId]?.handleMessagePart(messagePart) ?: LOGGER.trace {
                    "${messagePart.transferId.debugString()} unknown transfer"
                }
            }

            is Rldp2MessagePart.Confirm -> {
                transfers[messagePart.transferId]?.handleMessagePart(messagePart) ?: LOGGER.trace {
                    "${messagePart.transferId.debugString()} unknown transfer"
                }
            }

            is Rldp2MessagePart.Complete -> {
                transfers[messagePart.transferId]?.handleMessagePart(messagePart) ?: LOGGER.trace {
                    "${messagePart.transferId.debugString()} unknown transfer"
                }
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
        TL.Boxed.encodeToByteString(RldpMessage.serializer(), message)
    }

    @OptIn(ExperimentalTime::class)
    suspend fun query(
        data: ByteString,
        maxAnswerSize: Long = 1024 * 1024 * 5, // 5 MB
        timeout: Duration = defaultQueryTimeout
    ): ByteString = coroutineScope {
        val queryId = ByteString(*Random.nextBytes(32))


        val outgoingQueryParts = Channel<Rldp2MessagePart>()
        val queryTransferId = randomTransferId()
        val queryTransfer = RldpTransfer(
            id = queryTransferId,
            rldp = this@RldpConnection,
            outgoing = outgoingQueryParts
        )
        transfers[queryTransferId] = queryTransfer

        val outgoingAnswerParts = Channel<Rldp2MessagePart>()
        val answerTransferId = responseTransferId(queryTransferId)
        val answerTransfer = RldpTransfer(
            id = answerTransferId,
            rldp = this@RldpConnection,
            outgoing = outgoingAnswerParts
        )
        transfers[answerTransferId] = answerTransfer

        LOGGER.debug { "start transfer ${queryTransferId.debugString()} - ${answerTransferId.debugString()}" }

        val outgoingQueryPartsJob = launch {
            for (msg in outgoingQueryParts) {
                val rawMsg = TL.Boxed.encodeToByteString(Rldp2MessagePart.serializer(), msg)
                adnl.message(rawMsg)
            }
        }
        val outgoingAnswerPartsJob = launch {
            for (msg in outgoingAnswerParts) {
                val rawMsg = TL.Boxed.encodeToByteString(Rldp2MessagePart.serializer(), msg)
                LOGGER.trace { "${answerTransferId.debugString()} outgoingAnswerParts: $msg" }
                adnl.message(rawMsg)
            }
        }
        val answerDeferred = async {
            val rawAnswer = answerTransfer.receive()
            TL.Boxed.decodeFromByteString(RldpMessage.Answer.serializer(), rawAnswer).data
        }

        val query = RldpMessage.Query(
            queryId = queryId,
            maxAnswerSize = maxAnswerSize,
            timeout = (Clock.System.now() + timeout).epochSeconds.toInt(),
            data = data
        )
        val rawQuery = TL.Boxed.encodeToByteString(RldpMessage.serializer(), query)
        queryTransfer.send(rawQuery)
        LOGGER.info("=== DONE SENDING QUERY, WAITING FOR ANSWER ${answerTransferId.debugString()} ===")

        val answer = answerDeferred.await()
        LOGGER.info("GOT ANSWER, cancelling..")
        outgoingQueryPartsJob.cancelAndJoin()
        LOGGER.info("canceled outgoing query parts")
        outgoingAnswerPartsJob.cancelAndJoin()
        LOGGER.info("canceled answer query parts")
        answer
    }

//    private suspend fun <T> transferScope(
//        id: ByteString = randomTransferId(),
//        block: suspend RldpTransfer.() -> T
//    ): T {
//        val transfer = transfer(id)
//        return try {
//            LOGGER.trace { "start transfer ${id.debugString()}" }
//            transfer.block()
//        } finally {
//            transfers.remove(id)
//            LOGGER.trace { "end transfer ${id.debugString()}" }
//        }
//    }
//
//    private suspend fun <T> RldpTransfer.responseTransfer(
//        block: suspend RldpTransfer.() -> T
//    ): T {
//        val bytes = id.toByteArray()
//        for (i in 0 until 32) {
//            bytes[i] = bytes[i].xor(0xFF.toByte())
//        }
//        val responseId = ByteString(*bytes)
//        LOGGER.trace { "new receive transfer: ${id.debugString()} -> ${responseId.debugString()}" }
//        return transferScope(responseId, block)
//    }

    private fun responseTransferId(transferId: ByteString): ByteString {
        val bytes = transferId.toByteArray()
        for (i in 0 until 32) {
            bytes[i] = bytes[i].xor(0xFF.toByte())
        }
        return ByteString(*bytes)
    }

    private fun randomTransferId(): ByteString {
        return ByteString(*Random.nextBytes(32))
    }
}

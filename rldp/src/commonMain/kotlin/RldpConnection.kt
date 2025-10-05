package org.ton.kotlin.rldp

import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.io.IOException
import kotlinx.io.bytestring.ByteString
import kotlinx.io.readByteString
import org.ton.kotlin.adnl.*
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

    private val connections = Hash256Map<AdnlIdShort, CompletableDeferred<RldpConnection>>({ it.hash })

    suspend fun connection(adnlIdShort: AdnlIdShort): RldpConnection? {
        connections[adnlIdShort]?.let { return it.await() }

        val peer = adnlLocalNode.peer(adnlIdShort) ?: return null

        val slot = CompletableDeferred<RldpConnection>()
        val existing = connections.putIfAbsent(adnlIdShort, slot)
        if (existing != null) {
            return existing.await()
        }
        val connection = RldpConnection(peer)
        slot.complete(connection)
        return connection
    }

    suspend fun connection(adnlNode: AdnlNode): RldpConnection {
        val shortId = adnlNode.shortId
        connections[shortId]?.let { return it.await() }

        val peer = adnlLocalNode.peer(adnlNode)
        val slot = CompletableDeferred<RldpConnection>()
        val existing = connections.putIfAbsent(adnlNode.shortId, slot)
        if (existing != null) {
            return existing.await()
        }
        val connection = RldpConnection(peer)
        slot.complete(connection)
        return connection
    }

    suspend fun dial(id: AdnlIdShort, resolver: AdnlNodeResolver = AdnlNodeResolver { null }): RldpConnection {
        connection(id)?.let { return it }
        val adnlNode = resolver.resolveAdnlNode(id) ?: throw IOException("Can't resolve ADNL node: $id")
        return connection(adnlNode)
    }
}

typealias RldpQueryHandler = suspend RldpConnection.(query: RldpMessage.Query) -> Unit
typealias RldpMessageHandler = suspend RldpConnection.(query: RldpMessage.Custom) -> Unit

private val LOGGER = KtorSimpleLogger("org.ton.kotlin.rldp.RldpConnection")

class RldpConnection internal constructor(
    val adnl: AdnlPeerPair,
) : CoroutineScope {
    val defaultQueryTimeout = 15.seconds

    private val transfers = Hash256Map<ByteString, RldpTransfer>({ it })
    private val incomingTransfersJobs = Hash256Map<ByteString, Job>({ it })

    override val coroutineContext: CoroutineContext = adnl.coroutineContext + SupervisorJob()

    init {
        LOGGER.info("New RLDP connection: $adnl")
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
                getOrCreateIncomingTransfer(messagePart.transferId).handleMessagePart(messagePart)
            }

            is Rldp2MessagePart.Confirm -> {
                transfers[messagePart.transferId]?.handleMessagePart(messagePart) ?: LOGGER.trace {
                    "${messagePart.transferId.debugString()} unknown transfer, got $messagePart"
                }
            }

            is Rldp2MessagePart.Complete -> {
                transfers[messagePart.transferId]?.handleMessagePart(messagePart) ?: LOGGER.trace {
                    "${messagePart.transferId.debugString()} unknown transfer, got: $messagePart"
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun getOrCreateIncomingTransfer(transferId: ByteString): RldpTransfer {
        transfers[transferId]?.let { return@getOrCreateIncomingTransfer it }
        println("create new incoming transfer: $transferId")
        if (transfers.size >= 64) {
            val oldTransferEntry = transfers.minByOrNull { it.value.lastActive }
            if (oldTransferEntry != null) {
                LOGGER.debug { "remove old transfer: ${oldTransferEntry.key}" }
                transfers.remove(oldTransferEntry.key)
                incomingTransfersJobs.remove(oldTransferEntry.key)?.cancel()
            }
        }
        val outgoingQueryParts = Channel<Rldp2MessagePart>()
        val transfer = RldpTransfer(transferId, this, outgoingQueryParts)
        transfers[transferId] = transfer
        incomingTransfersJobs[transferId] = launch {
            val outgoingJob = launch {
                for (part in outgoingQueryParts) {
                    sendMessagePart(part)
                    LOGGER.trace { "sent outgoing for unknown ${transfer.id.debugString()} - $part" }
                }
            }
            val result = transfer.receive()
            LOGGER.trace { "${transfer.id.debugString()} received data from unknown transfer" }
            outgoingJob.cancelAndJoin()
            runCatching {
                TL.Boxed.decodeFromByteString<RldpMessage>(result)
            }.onSuccess {
                when (it) {
                    is RldpMessage.Answer -> {
                        LOGGER.warn("transfer=${transferId.debugString()} Unexpected answer for transfer, $it")
                    }

                    is RldpMessage.Custom -> {
                        LOGGER.warn("transfer=${transferId.debugString()} Unhandled message $it")
                    }

                    is RldpMessage.Query -> {
                        LOGGER.debug { "transfer=${transferId.debugString()}, incoming query: $it" }
                        launch {
                            val channel = ByteChannel()
                            adnl.processQuery(it.data, channel)
                            sendAnswer(
                                transferId,
                                RldpMessage.Answer(it.queryId, channel.readBuffer().readByteString())
                            )
                        }
                    }
                }
            }.onFailure {
                LOGGER.warn("Failed to decode RLDP message for transfer: $transferId", it)
            }
        }
        return transfer
    }

    suspend fun sendAnswer(
        transferId: ByteString,
        answer: RldpMessage.Answer
    ) = coroutineScope {
        val outgoingQueryParts = Channel<Rldp2MessagePart>()
        val answerTransferId = responseTransferId(transferId)
        val queryTransfer = RldpTransfer(
            id = answerTransferId,
            rldp = this@RldpConnection,
            outgoing = outgoingQueryParts
        )
        transfers[answerTransferId] = queryTransfer
        LOGGER.trace { "sending answer for: ${transferId.debugString()}, register: ${answerTransferId.debugString()}" }

        val outgoingQueryPartsJob = launch {
            LOGGER.trace { "${transferId.debugString()} -> ${answerTransferId.debugString()} -> start sending parts" }
            for (msg in outgoingQueryParts) {
                sendMessagePart(msg)
            }
        }

        val rawAnswer = TL.Boxed.encodeToByteString(RldpMessage.serializer(), answer)
        queryTransfer.send(rawAnswer)
        outgoingQueryPartsJob.cancelAndJoin()
    }

    internal suspend fun sendMessagePart(msg: Rldp2MessagePart) {
        val rawMsg = TL.Boxed.encodeToByteString(Rldp2MessagePart.serializer(), msg)
        adnl.message(rawMsg)
    }

    suspend fun query(
        data: ByteArray,
        maxAnswerSize: Long = 1024 * 1024 * 5, // 5 MB
        timeout: Duration = defaultQueryTimeout
    ): ByteArray = query(ByteString(*data), maxAnswerSize, timeout).toByteArray()

    @OptIn(ExperimentalTime::class)
    suspend fun query(
        data: ByteString,
        maxAnswerSize: Long = 1024 * 1024 * 5, // 5 MB
        timeout: Duration = defaultQueryTimeout
    ): ByteString {
        val queryId = ByteString(*Random.nextBytes(32))

        return withContext(CoroutineName("rldp-query-${queryId.debugString()}")) {
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

            LOGGER.info("start query transfer ${queryTransferId.debugString()} -> ${answerTransferId.debugString()}")

            val queryJob = launch {
                val outgoingQueryPartsJob = launch {
                    for (msg in outgoingQueryParts) {
                        sendMessagePart(msg)
                    }
                }
                val query = RldpMessage.Query(
                    queryId = queryId,
                    maxAnswerSize = maxAnswerSize,
                    timeout = (Clock.System.now() + timeout).epochSeconds.toInt(),
                    data = data
                )
                val rawQuery = TL.Boxed.encodeToByteString(RldpMessage.serializer(), query)
                withTimeout(timeout) {
                    queryTransfer.send(rawQuery)
                }
                outgoingQueryPartsJob.cancelAndJoin()
            }
            val answerDeferred = async {
                val outgoingAnswerPartsJob = launch {
                    for (msg in outgoingAnswerParts) {
                        sendMessagePart(msg)
                    }
                }
                val rawAnswer = withTimeout(timeout) {
                    answerTransfer.receive()
                }
                LOGGER.trace { "${answerTransfer.id.debugString()} received answer for query" }
                outgoingAnswerPartsJob.cancelAndJoin()
                TL.Boxed.decodeFromByteString(RldpMessage.Answer.serializer(), rawAnswer).data
            }
            val answer = answerDeferred.await()
            queryJob.cancelAndJoin()
            answer
        }
    }

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

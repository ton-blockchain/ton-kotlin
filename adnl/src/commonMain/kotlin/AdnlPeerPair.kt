package org.ton.kotlin.adnl

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.withTimeout
import kotlinx.io.Buffer
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encode
import kotlinx.io.write
import kotlinx.serialization.encodeToByteArray
import org.ton.kotlin.adnl.channel.AdnlChannel
import org.ton.kotlin.adnl.message.AdnlMessage
import org.ton.kotlin.adnl.util.Hash256Map
import org.ton.kotlin.crypto.PrivateKeyEd25519
import org.ton.kotlin.crypto.PublicKeyEd25519
import org.ton.kotlin.tl.TL
import kotlin.coroutines.CoroutineContext
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

typealias AdnlMessageHandler = suspend AdnlPeerPair.(message: AdnlMessage.Custom) -> Unit

@OptIn(ExperimentalTime::class)
class AdnlPeerPair(
    val localNode: AdnlLocalNode,
    val remoteId: AdnlIdFull,
    initialAddress: AdnlAddress,
) : CoroutineScope {
    private var channelKey = PrivateKeyEd25519.random()
    private var reinitDate = Clock.System.now()
    var channel: AdnlChannel? = null
        private set
    private var activeAddress = initialAddress
    private val outgoingQueries = Hash256Map<CompletableDeferred<ByteString>>()
    private val receiverState = PacketsHistory.forReceiver()
    private val senderState = PacketsHistory.forSender()
    private val encryptor = remoteId.publicKey.createEncryptor()
    private val defaultQueryTimeout = 5.seconds
    override val coroutineContext: CoroutineContext = localNode.coroutineContext

    private val _messageHandler = atomic<AdnlMessageHandler>({})

    private val _incomingQueries = Channel<AdnlMessage.Query>(0, BufferOverflow.DROP_OLDEST)
    val incomingQueries: ReceiveChannel<AdnlMessage.Query> = _incomingQueries

    fun onAdnlMessage(handler: AdnlMessageHandler) {
        _messageHandler.value = handler
    }

    suspend fun sendMessage(message: AdnlMessage) {
        val channel = channel

        val packet = AdnlPacketBuilder().apply { initRandom() }
        var encryptor = encryptor
        val viaChannel: Boolean = channel != null && channel.isReady
        when {
            channel == null -> {
                packet.from = localNode.id
                packet.messages.add(
                    AdnlMessage.CreateChannel(
                        key = channelKey.publicKey(),
                        date = Clock.System.now().epochSeconds.toInt()
                    )
                )
            }

            !channel.isReady -> {
                packet.from = localNode.id
                packet.messages.add(
                    AdnlMessage.ConfirmChannel(
                        key = channelKey.publicKey(),
                        peerKey = channel.remoteKey,
                        date = Clock.System.now().epochSeconds.toInt()
                    )
                )
            }

            else -> {
                encryptor = channel.encryptor
            }
        }
        packet.messages.add(message)
        packet.addressList = AdnlAddressList()
        packet.seqno = senderState.incrementSeqno()
        packet.confirmSeqno = receiverState.seqno()
        val totalSize = packet.messages.sumOf { it.size }
        val MAX_ADNL_MESSAGE_SIZE = 1024
        if (totalSize <= MAX_ADNL_MESSAGE_SIZE) {
            val packet = if (viaChannel) {
                packet.build()
            } else {
                packet.build().signed(localNode.key)
            }
            val serializedPacket = TL.Boxed.encodeToByteArray(packet)
            val encryptedPacket = encryptor.encryptToByteArray(serializedPacket)
            val datagram = Buffer()
            if (viaChannel) {
                datagram.write(channel.outId.hash)
            } else {
                datagram.write(remoteId.idShort.hash)
            }
            datagram.write(encryptedPacket)
            localNode.sendDatagram(datagram, activeAddress)
        } else {
            // TODO: multi part message
            throw IllegalStateException("Message size exceeds maximum allowed size: $totalSize > $MAX_ADNL_MESSAGE_SIZE")
        }
    }

    suspend fun query(
        query: ByteArray,
        timeout: Duration = defaultQueryTimeout
    ): ByteArray = query(ByteString(query), timeout).toByteArray()

    suspend fun query(
        query: ByteString,
        timeout: Duration = defaultQueryTimeout
    ): ByteString {
        val id = ByteString(*Random.nextBytes(32))
        val deferred = CompletableDeferred<ByteString>().apply {
            invokeOnCompletion {
                outgoingQueries.remove(id)
            }
        }
        outgoingQueries[id] = deferred
        sendMessage(AdnlMessage.Query(id, query))
        val result = withTimeout(timeout) {
            deferred.await()
        }
        return result
    }

    suspend fun message(
        data: ByteArray
    ) = message(ByteString(data))

    suspend fun message(
        data: ByteString
    ) = sendMessage(AdnlMessage.Custom(data))

    suspend fun processPacket(packet: AdnlPacket, checkSignature: Boolean) {
//        println("${remoteId.idShort} packet=$packet")
        if (checkSignature && !packet.isValidSignature()) {
            println("Invalid signature for packet: ${packet.seqno} from ${packet.from?.idShort?.hash}")
            return
        }
        packet.seqno?.let { seqno ->
            if (!receiverState.deliverPacket(seqno)) {
                return
            }
        }
        packet.confirmSeqno?.let { confirmSeqno ->
            if (confirmSeqno > 0 && confirmSeqno > senderState.seqno()) {
                return
            }
        }
        packet.message?.let { processMessage(it) }
        packet.messages?.forEach { processMessage(it) }
    }

    private fun AdnlPacket.isValidSignature(): Boolean {
        val signature = this.signature ?: return false
        val raw = TL.Boxed.encodeToByteArray(toSign())
        return encryptor.checkSignature(raw, signature.toByteArray())
    }

    suspend fun processMessage(message: AdnlMessage) {
        when (message) {
            is AdnlMessage.Query -> {
                _incomingQueries.send(message)
            }

            is AdnlMessage.Answer -> {
                val queryId = message.queryId
                val deferred = outgoingQueries[queryId] ?: return
                deferred.complete(message.answer)
            }

            is AdnlMessage.ConfirmChannel -> {
                if (message.peerKey != channelKey.publicKey()) {
                    println("Received confirm for unexpected channel key: ${message.peerKey} != $channelKey")
                    return
                }
                createChannel(
                    message.key,
                    date = Instant.fromEpochSeconds(message.date.toUInt().toLong()),
                    isReady = true
                )
            }

            is AdnlMessage.CreateChannel -> createChannel(
                message.key,
                date = Instant.fromEpochSeconds(message.date.toUInt().toLong()),
                isReady = false
            )

            is AdnlMessage.Custom -> {
                try {
                    _messageHandler.value(this, message)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }

            AdnlMessage.Nop -> {
                // No operation, do nothing
            }

            is AdnlMessage.Part -> {
                println("Received part message: $message")
//                TODO()
            }

            is AdnlMessage.Reinit -> TODO()

        }
    }

    private fun createChannel(
        key: PublicKeyEd25519,
        date: Instant,
        isReady: Boolean
    ) {
        val currentChannel = channel
        if (currentChannel != null && currentChannel.isReady) {
            if (currentChannel.remoteKey == key) {
                return
            }
            if (date <= currentChannel.date) {
                return
            }
        }
        channel = AdnlChannel.create(this, channelKey, key, date, isReady)
//        println("${remoteId.idShort} Created channel: $channel")
    }

    @OptIn(ExperimentalEncodingApi::class)
    override fun toString(): String =
        "AdnlPeerPair(${Base64.encode(localNode.shortId.hash)}->${Base64.encode(remoteId.idShort.hash)})"

}

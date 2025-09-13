package org.ton.kotlin.adnl

import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.io.Buffer
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encode
import kotlinx.io.readByteString
import kotlinx.io.write
import kotlinx.serialization.encodeToByteArray
import org.ton.kotlin.adnl.channel.AdnlChannel
import org.ton.kotlin.adnl.message.AdnlMessage
import org.ton.kotlin.adnl.message.AdnlMessageAssembler
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
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

typealias AdnlMessageHandler = suspend AdnlPeerPair.(message: AdnlMessage.Custom) -> Unit

@OptIn(ExperimentalTime::class)
class AdnlPeerPair(
    val localNode: AdnlLocalNode,
    val remoteNode: AdnlNode,
    private val queries: MutableSharedFlow<AdnlQuery>
) : CoroutineScope {
    private val logger = KtorSimpleLogger("AdnlPeerPair")
    private var channelKey = PrivateKeyEd25519.random()
    private var reinitDate = Clock.System.now()
    private var peerReinitDate: Instant = Instant.DISTANT_PAST
    var channel: AdnlChannel? = null
        private set
    private var addressList = remoteNode.addrList
    private var observedAddress: AdnlAddress? = null
    private val outgoingQueries = Hash256Map<ByteString, CompletableDeferred<ByteString>>({ it })
    private var receiverState = PacketsHistory.forReceiver()
    private var senderState = PacketsHistory.forSender()
    private val encryptor = remoteNode.publicKey.createEncryptor()
    private val defaultQueryTimeout = 5.seconds
    private val messageAssembler = AdnlMessageAssembler()
    override val coroutineContext: CoroutineContext = localNode.coroutineContext

    private val _messageHandler = atomic<AdnlMessageHandler>({})

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
                packet.addressList = AdnlAddressList(
                    version = Clock.System.now().epochSeconds.toInt(),
                    reinitDate = reinitDate.epochSeconds.toInt()
                )
                packet.from = localNode.id
                packet.reinitDate = reinitDate
                packet.dstReinitDate = peerReinitDate
                packet.messages.add(
                    AdnlMessage.CreateChannel(
                        key = channelKey.publicKey(),
                        date = Clock.System.now().epochSeconds.toInt()
                    )
                )
            }

            !channel.isReady -> {
                packet.addressList = AdnlAddressList(
                    version = Clock.System.now().epochSeconds.toInt(),
                    reinitDate = reinitDate.epochSeconds.toInt()
                )
                packet.from = localNode.id
                packet.reinitDate = reinitDate
                packet.dstReinitDate = peerReinitDate
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
            packet.message?.let {
                logger.trace { "${remoteNode.shortId} outgoing message ${if (viaChannel) "viaChannel" else ""}: $it" }
            }
            packet.messages?.forEach {
                logger.trace { "${remoteNode.shortId} outgoing message ${if (viaChannel) "viaChannel" else ""}: $it" }
            }
            val serializedPacket = TL.Boxed.encodeToByteArray(packet)
            val encryptedPacket = encryptor.encryptToByteArray(serializedPacket)
            val datagram = Buffer()
            if (viaChannel) {
                datagram.write(channel.outId.hash)
            } else {
                datagram.write(remoteNode.shortId.hash)
            }
            datagram.write(encryptedPacket)

            val observedAddress = observedAddress
            if (addressList.addrs.isEmpty()) {
                if (observedAddress != null) {
                    logger.trace { "${remoteNode.shortId} send datagram to observed address: $observedAddress" }
                    localNode.sendDatagram(datagram, observedAddress)
                } else {
                    throw IllegalStateException("Unknown destination address for ${remoteNode.shortId}")
                }
            } else {
                addressList.addrs.map { address ->
                    launch {
                        logger.trace { "${remoteNode.shortId} send datagram to address: $address" }
                        localNode.sendDatagram(datagram, address)
                    }
                }.joinAll()
            }
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

    suspend fun processQuery(data: ByteString, output: ByteWriteChannel) {
        val query = AdnlQuery(this@AdnlPeerPair, data, output)
        queries.emit(query)
    }

    suspend fun processPacket(packet: AdnlPacket, checkSignature: Boolean, observedAddress: AdnlAddress) {
        if (checkSignature && !packet.isValidSignature()) {
            println("Invalid signature for packet: ${packet.seqno} from ${packet.from?.shortId?.hash}")
            return
        }
        val currentObservedAddress = this.observedAddress
        if (currentObservedAddress != observedAddress) {
            this.observedAddress = observedAddress
            logger.trace { "new observed address: $observedAddress" }
        }
        val packetDstReinitDate =
            packet.dstReinitDate?.let { Instant.fromEpochSeconds(it.toLong()) } ?: Instant.DISTANT_PAST
        if (packetDstReinitDate > reinitDate) {
            logger.debug { "drop: too new our reinit date" }
            return
        }

        packet.reinitDate?.let {
            val packetReinitDate = Instant.fromEpochSeconds(it.toLong())
            if (packetReinitDate > Clock.System.now() + 1.minutes) {
                logger.debug { "drop: too new reinit date" }
                return
            }
            if (packetReinitDate > peerReinitDate) {
                reinit(packetReinitDate)
            }
            if (packetReinitDate < peerReinitDate) {
                logger.debug { "drop: too old peer reinit date: $packetReinitDate, last reinit: $peerReinitDate" }
                return
            }
        }

        packet.address?.let {
            logger.trace { "new address list: $it" }
            updateAddrList(it)
        }
//        logger.debug { "[${remoteNode.shortId}] incoming packet: seqno:${packet.seqno}, ack:${packet.confirmSeqno}" }
        packet.seqno?.let { seqno ->
            if (!receiverState.deliverPacket(seqno)) {
//                logger.debug { "[${remoteNode.shortId}] drop: old seqno: $seqno (current max ${receiverState.seqno()})" }
                return
            }
        }
        packet.confirmSeqno?.let { confirmSeqno ->
            if (confirmSeqno > 0 && confirmSeqno > senderState.seqno()) {
                logger.debug { "drop: new ack seqno: $confirmSeqno (current max sent ${senderState.seqno()})" }
                return
            }
        }
        packet.message?.let { processMessage(it) }
        packet.messages?.forEach { processMessage(it) }
    }

    private fun updateAddrList(addressList: AdnlAddressList) {
        if (addressList.reinitDate > (Clock.System.now() + 1.minutes).epochSeconds) {
            return
        }
        if (addressList.reinitDate > peerReinitDate.epochSeconds) {
            reinit(Instant.fromEpochSeconds(addressList.reinitDate.toLong()))
        } else if (addressList.reinitDate < peerReinitDate.epochSeconds) {
            return
        }

        this.addressList = addressList
    }

    private fun reinit(date: Instant) {
        val current = peerReinitDate
        if (current == Instant.DISTANT_PAST) {
            peerReinitDate = date
        } else if (current < date) {
            logger.debug { "new peer reinit date: $date, current: $current" }
            peerReinitDate = date
            receiverState = PacketsHistory.forReceiver()
            senderState = PacketsHistory.forSender()
            reinitDate = Clock.System.now()
            channelKey = PrivateKeyEd25519.random()
            logger.info("new init channel key: ${channelKey.publicKey()}")
            channel = null
        }
    }

    private fun AdnlPacket.isValidSignature(): Boolean {
        val signature = this.signature ?: return false
        val raw = TL.Boxed.encodeToByteArray(toSign())
        return encryptor.checkSignature(raw, signature.toByteArray())
    }

    suspend fun processMessage(message: AdnlMessage) {
        logger.trace { "${remoteNode.shortId} incoming message: $message" }
        when (message) {
            is AdnlMessage.Query -> {
                launch {
                    withTimeout(10.seconds) {
                        val output = ByteChannel()
                        processQuery(message.query, output)
                        val answer = output.readRemaining().readByteString()
                        sendMessage(AdnlMessage.Answer(message.queryId, answer))
                    }
                }
            }

            is AdnlMessage.Answer -> {
                val queryId = message.queryId
                val deferred = outgoingQueries[queryId] ?: return
                deferred.complete(message.answer)
            }

            is AdnlMessage.ConfirmChannel -> {
                if (message.peerKey != channelKey.publicKey()) {
                    logger.warn("[${remoteNode.shortId}] Received confirm for unexpected channel key: ${message.peerKey} != ${channelKey.publicKey()}")
                    sendMessage(AdnlMessage.Nop)
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
                messageAssembler.accept(message)?.let {
                    processMessage(it)
                }
            }

            is AdnlMessage.Reinit -> {
                reinit(Instant.fromEpochSeconds(message.date.toLong()))
            }
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
        logger.debug { "${remoteNode.shortId} Created channel: $channel" }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override fun toString(): String =
        "AdnlPeerPair(${Base64.encode(localNode.shortId.hash)}->${Base64.encode(remoteNode.shortId.hash)})"

}

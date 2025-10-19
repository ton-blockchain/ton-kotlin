package org.ton.kotlin.adnl.internal

import io.ktor.util.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encode
import kotlinx.io.readByteString
import kotlinx.serialization.encodeToByteArray
import org.ton.kotlin.adnl.*
import org.ton.kotlin.adnl.message.AdnlMessage
import org.ton.kotlin.adnl.message.AdnlMessageAssembler
import org.ton.kotlin.adnl.util.Hash256Map
import org.ton.kotlin.crypto.*
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

private const val CHANNEL_PACKET_HEADER_MAX_SIZE = 128
private const val PACKET_HEADER_MAX_SIZE = 272
private const val ADNL_MTU = 1440

@OptIn(ExperimentalTime::class)
internal class AdnlPeerPair internal constructor(
    override val localNode: AdnlLocalNodeImpl,
    override val remoteNode: AdnlNode,
    private val incoming: ReceiveChannel<Pair<AdnlPacket, AdnlAddress>>,
    private val messageHandler: List<AdnlMessageHandler>,
    private val queryHandlers: List<AdnlQueryHandler>
) : CoroutineScope, AdnlChannel {
    private val logger = KtorSimpleLogger("AdnlPeerPair")

    override val attributes: Attributes = Attributes()

    private var channelKey = PrivateKeyEd25519.random()
    private var channelDate = Clock.System.now()
    private var channel: InitializedChannel? = null

    private var reinitDate = Clock.System.now()
    private var peerReinitDate: Instant = Instant.DISTANT_PAST
    private var addressList = remoteNode.addresses
    private lateinit var observedAddress: AdnlAddress
    private val outgoingQueries = Hash256Map<ByteString, CompletableDeferred<ByteString>>({ it })
    private var receiverState = PacketsHistory.forReceiver()
    private var senderState = PacketsHistory.forSender()
    private val encryptor = remoteNode.publicKey
    private val messageAssembler = AdnlMessageAssembler()

    override val coroutineContext: CoroutineContext =
        localNode.coroutineContext + Job(localNode.coroutineContext.job)

    private val queryJob = SupervisorJob(coroutineContext.job)

    private val incomingConfirmChannelMessages = Channel<AdnlMessage.ConfirmChannel>()
    private val outgoingMessages = Channel<AdnlMessage>(1)

    init {
        launch(CoroutineName("adnl-receive-loop")) {
            while (true) {
                val (packet, address) = incoming.receive()
                processPacket(packet, true, address)
            }
        }
        launch(CoroutineName("adnl-messages-send-loop")) {
            var message = outgoingMessages.receive()
            while (true) {
                val channel = channel
                val viaChannel = channel?.isReady ?: false
                var totalSize = if (viaChannel) CHANNEL_PACKET_HEADER_MAX_SIZE else PACKET_HEADER_MAX_SIZE

                val packet = AdnlPacketBuilder().apply { initRandom() }
                if (channel == null) {
                    val createChannelMsg = AdnlMessage.CreateChannel(
                        channelKey.publicKey(),
                        channelDate.epochSeconds.toInt()
                    )
                    packet.messages.add(createChannelMsg)
                    totalSize += createChannelMsg.size
                } else if (!channel.isReady && message !is AdnlMessage.ConfirmChannel) {
                    val confirmChannel = AdnlMessage.ConfirmChannel(
                        key = channelKey.publicKey(),
                        peerKey = channel.remoteKey,
                        date = channelDate.epochSeconds.toInt()
                    )
                    packet.messages.add(confirmChannel)
                    totalSize += confirmChannel.size
                }

                if (totalSize + message.size <= ADNL_MTU) {
                    packet.messages.add(message)
                } else {
                    // skip big messages
                }

                while (true) {
                    val nextMessage = outgoingMessages.tryReceive().getOrNull() ?: break
                    if (totalSize + nextMessage.size <= ADNL_MTU) {
                        totalSize += nextMessage.size
                        packet.messages.add(nextMessage)
                    } else {
                        message = nextMessage
                        break
                    }
                }

                packet.seqno = senderState.incrementSeqno()
                packet.confirmSeqno = receiverState.seqno()
                if (viaChannel) {
                    val datagram = AdnlDatagram(
                        dest = remoteNode.shortId,
                        address = observedAddress,
                        packet = TL.Boxed.encodeToByteArray(packet.build())
                    )
                    channel.outgoing.send(datagram)
                } else {
                    packet.from = localNode.id
                    val signedPacket = TL.Boxed.encodeToByteArray(packet.build().signed(localNode.key))
                    addressList.map { address ->
                        launch {
                            val datagram = AdnlDatagram(
                                dest = remoteNode.shortId,
                                address = address,
                                packet = signedPacket.copyOf()
                            )
                            localNode.adnl.sendDatagram(datagram)
                        }
                    }.joinAll()
                }
            }
        }
    }

    override suspend fun sendQuery(
        query: ByteString,
        timeout: Duration
    ): ByteString {
        val id = ByteString(*Random.nextBytes(32))
        val deferred = CompletableDeferred<ByteString>().apply {
            invokeOnCompletion {
                outgoingQueries.remove(id)
            }
        }
        outgoingQueries[id] = deferred
        outgoingMessages.send(AdnlMessage.Query(id, query))
        val result = withTimeout(timeout) {
            deferred.await()
        }
        return result
    }

    override suspend fun sendMessage(
        data: ByteString
    ) {
        outgoingMessages.send(AdnlMessage.Custom(data))
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
        return encryptor.verifySignature(raw, signature.toByteArray())
    }

    suspend fun processMessage(message: AdnlMessage) {
        logger.trace { "${remoteNode.shortId} incoming message: $message" }
        when (message) {
            is AdnlMessage.Query -> {
                launch(queryJob) {
                    withTimeout(10.seconds) {
                        val output = ByteChannel()
                        val query = AdnlQuery(this@AdnlPeerPair, message.query, output)
                        queryHandlers.forEach {
                            it(this@AdnlPeerPair, query)
                        }
                        val answer = output.readRemaining().readByteString()
                        outgoingMessages.send(AdnlMessage.Answer(message.queryId, answer))
                    }
                }
            }

            is AdnlMessage.Answer -> {
                val queryId = message.queryId
                val deferred = outgoingQueries[queryId] ?: return
                deferred.complete(message.answer)
            }

            is AdnlMessage.ConfirmChannel -> {
                createChannel(
                    channelKey,
                    message.key,
                    Instant.fromEpochSeconds(message.date.toUInt().toLong()),
                )
            }

            is AdnlMessage.CreateChannel -> {
                createChannel(
                    channelKey,
                    message.key,
                    Instant.fromEpochSeconds(message.date.toUInt().toLong()),
                )
                outgoingMessages.send(
                    AdnlMessage.ConfirmChannel(
                        key = channelKey.publicKey(),
                        peerKey = message.key,
                        date = Clock.System.now().epochSeconds.toInt()
                    )
                )
            }

            is AdnlMessage.Custom -> {
                launch(queryJob) {
                    messageHandler.forEach {
                        it(this@AdnlPeerPair, message.data)
                    }
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
        localKey: PrivateKeyEd25519,
        key: PublicKeyEd25519,
        date: Instant,
    ) {
        val currentChannel = channel
        if (currentChannel != null) {
            if (currentChannel.remoteKey == key) {
                return
            }
//            if (date <= currentChannel.date) {
//                return
//            }
        }

        val sharedSecret = localKey.computeSharedSecret(key)
        val sharedSecretByteString = ByteString(*sharedSecret)
        val decryptSecret: PrivateKeyAes
        val encryptSecret: PublicKeyAes
        val compared = localNode.shortId.compareTo(remoteNode.shortId)
        if (compared == 0) {
            decryptSecret = PrivateKeyAes(sharedSecretByteString)
            encryptSecret = PublicKeyAes(sharedSecretByteString)
        } else {
            val reversedSecret = ByteString(*sharedSecret.reversedArray())
            if (compared < 0) {
                decryptSecret = PrivateKeyAes(sharedSecretByteString)
                encryptSecret = PublicKeyAes(reversedSecret)
            } else {
                decryptSecret = PrivateKeyAes(reversedSecret)
                encryptSecret = PublicKeyAes(sharedSecretByteString)
            }
        }

        val input = Channel<AdnlDatagram>()
        val output = Channel<AdnlDatagram>()

        val newChannel = InitializedChannel(
            this,
            input,
            output,
            key,
            AdnlIdShort(encryptSecret.computeShortId()),
            AdnlIdShort(decryptSecret.computeShortId()),
            encryptSecret,
            decryptSecret,
        )
        localNode.adnl.channels[newChannel.incomingId] = input
        newChannel.launch {
            while (true) {
                val datagram = output.receive()
                localNode.adnl.sendDatagram(datagram)
            }
        }
        newChannel.coroutineContext.job.invokeOnCompletion {
            localNode.adnl.channels.remove(newChannel.incomingId)
        }

        channel = newChannel
        currentChannel?.cancel()

        logger.debug { "${remoteNode.shortId} Created channel: $channel" }
    }

    internal class InitializedChannel(
        val peerPair: AdnlPeerPair,
        rawInput: ReceiveChannel<AdnlDatagram>,
        rawOutput: SendChannel<AdnlDatagram>,
        val remoteKey: PublicKeyEd25519,
        val outgoingId: AdnlIdShort,
        val incomingId: AdnlIdShort,
        val encryptor: Encryptor,
        val decryptor: Decryptor,
    ) : CoroutineScope {
        private val job = Job(peerPair.coroutineContext[Job])
        override val coroutineContext: CoroutineContext =
            peerPair.coroutineContext + job + CoroutineName("initialized-channel")

        private val _outgoing = Channel<AdnlDatagram>()
        private val _incoming = Channel<AdnlDatagram>()

        val outgoing: SendChannel<AdnlDatagram> = _outgoing

        private val _isReady = CompletableDeferred<Unit>()

        val isReady: Boolean get() = _isReady.isCompleted

        init {
            launch(CoroutineName("channel-encryptor")) {
                while (true) {
                    val datagram = _outgoing.receive()
                    encryptor.encryptIntoByteArray(datagram.packet, datagram.packet)
                    rawOutput.send(AdnlDatagram(outgoingId, datagram.address, datagram.packet))
                }
            }
            launch(CoroutineName("channel-decryptor")) {
                while (true) {
                    val datagram = rawInput.receive()
                    decryptor.decryptIntoByteArray(datagram.packet, datagram.packet)
                    if (!_isReady.isCompleted) {
                        _isReady.complete(Unit)
                    }
                    _incoming.send(AdnlDatagram(peerPair.localNode.shortId, datagram.address, datagram.packet))
                }
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override fun toString(): String =
        "AdnlPeerPair(${Base64.encode(localNode.shortId.hash)}->${Base64.encode(remoteNode.shortId.hash)})"
}

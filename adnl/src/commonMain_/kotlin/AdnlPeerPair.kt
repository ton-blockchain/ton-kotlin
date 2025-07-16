package org.ton.kotlin.adnl

import io.ktor.utils.io.core.writeFully
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import kotlinx.io.Buffer
import org.ton.kotlin.adnl.channel.AdnlChannel
import org.ton.kotlin.adnl.transport.AdnlTransport
import org.ton.kotlin.adnl.message.AdnlMessage
import org.ton.kotlin.crypto.PrivateKeyEd25519
import org.ton.kotlin.crypto.PublicKeyEd25519
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
abstract class AdnlPeerPair(
    val peerTable: AdnlPeerTable,
    val localId: AdnlIdShort,
    val remoteId: AdnlIdShort,
//    val transport: AdnlTransport,
    val remoteKey: PublicKeyEd25519,
) : CoroutineScope {
    override val coroutineContext: CoroutineContext get() = peerTable.coroutineContext

    private val channelPrivateKey = PrivateKeyEd25519.random()
    private val channelPublicKey by lazy { channelPrivateKey.publicKey() }
    private val channelDate = Clock.System.now()

    private val incomingMessages = Channel<AdnlMessage>(1)
    private val outgoingMessages = Channel<AdnlMessage>(10)
    private var channel: AdnlChannel? = null
    private var channelReady: Boolean = false
    private var tryReinitAt = Instant.DISTANT_FUTURE
    private var outSeqno = 0L
    private var inSeqno = 0L
    private var skipInitPacket = false


    private suspend fun receiveAndProcessPacket() {
        flushOutgoingMessages()

        select {
            channel?.incoming?.onReceive {
                processIncomingPacket(it, true)
            }

        }

//        select {
//            channel?.outgoingDatagrams?.onReceive {
//                peerTable.sendDatagram(it)
//            }
//            channel?.incomingPackets?.onReceive {
//                processIncomingPacket(it, false)
//            }
//            incomingPackets.onReceive {
//                processIncomingPacket(it, true)
//            }
//        }
    }

    suspend fun onPacket(packet: AdnlPacket) {
//        incomingPackets.send(packet)
    }

    private suspend fun processIncomingPacket(packet: AdnlPacket, isChannel: Boolean) {
        packetStats(packet, isIncoming = true, isChannel = isChannel)
        if (channel != null && !channelReady) {
            channelReady = true
        }
        packet.messages.forEach {
            incomingMessages.send(it)
        }
    }

    private suspend fun flushOutgoingMessages() {
        val messageQueue = ArrayDeque<AdnlMessage>()

        fun takeMessage(check: (AdnlMessage) -> Boolean): AdnlMessage? {
            if (messageQueue.isNotEmpty()) {
                val message = messageQueue.first()
                return if (check(message)) {
                    messageQueue.removeFirst()
                } else {
                    null
                }
            }
            val message = outgoingMessages.tryReceive().getOrNull()
            if (message != null) {
                if (check(message)) {
                    return message
                } else {
                    messageQueue.addLast(message)
                }
            }
            return null
        }

        while (true) {
            val tryReinit = Clock.System.now() >= tryReinitAt
            val viaChannel = channelReady && !tryReinit
            if (tryReinit) {
                tryReinitAt = Clock.System.now() + 1.seconds
            }
            val packetMessages = ArrayList<AdnlMessage>(2)
            val channel = channel
            if (channel == null) {
                val message = AdnlMessage.CreateChannel(
                    channelPublicKey,
                    channelDate
                )
                packetMessages.add(message)
            } else if (!channelReady) {
                val message = AdnlMessage.ConfirmChannel(
                    channel.localKey.publicKey(),
                    channel.remoteKey,
                    channel.date
                )
                packetMessages.add(message)
            }
            while (true) {
                val message = takeMessage { packetMessages.size < 2 } ?: break
                packetMessages.add(message)
            }
            val packet = AdnlPacket(
                messages = packetMessages,
                seqno = outSeqno++,
                confirmSeqno = inSeqno,
            )
            if (viaChannel && channel != null) {
                channel.send(packet)
            } else {
                val serialize = AdnlPacket.serialize(packet)
                val buffer = Buffer()
                buffer.writeFully(serialize)
//                transport.send(buffer)
            }
        }
    }

    suspend fun receiveMessage(): AdnlMessage = incomingMessages.receive()

    private fun createChannel(
        publicKey: PublicKeyEd25519,
        date: Instant
    ): AdnlChannel {
        var currentChannel = channel
        if (currentChannel != null) {
            if (currentChannel.remoteKey == publicKey) return currentChannel
            if (date <= currentChannel.date) return currentChannel
            channel = null
            channelReady = false
            peerTable.removeChannel(currentChannel.inputId)
        }
//        currentChannel = AdnlChannel.create(
//            channelPrivateKey,
//            publicKey,
//            localId,
//            remoteId
//        )
        channel = currentChannel
//        return currentChannel
        TODO()
    }

    private fun packetStats(packet: AdnlPacket, isIncoming: Boolean, isChannel: Boolean) {

    }
}

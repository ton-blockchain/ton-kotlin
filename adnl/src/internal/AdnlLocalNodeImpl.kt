package org.ton.kotlin.adnl.internal

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.decodeFromByteArray
import org.ton.kotlin.adnl.*
import org.ton.kotlin.adnl.util.Hash256Map
import org.ton.kotlin.crypto.PrivateKeyEd25519
import org.ton.kotlin.tl.TL
import kotlin.coroutines.CoroutineContext

internal class AdnlLocalNodeImpl internal constructor(
    val key: PrivateKeyEd25519,
    val adnl: Adnl,
    private val incomingDatagram: ReceiveChannel<AdnlDatagram>,
    private val messageHandlers: List<suspend AdnlChannel.(ByteString) -> Unit>,
    private val queryHandlers: List<suspend AdnlChannel.(AdnlQuery) -> Unit>
) : AdnlLocalNode {
    override val id: AdnlIdFull = AdnlIdFull(key.publicKey())

    override val coroutineContext: CoroutineContext = adnl.coroutineContext + SupervisorJob()
    private val channels =
        Hash256Map<AdnlIdShort, Channel<Pair<AdnlPacket, AdnlAddress>>> { it.hash }

    private val _newChannels = MutableSharedFlow<AdnlChannel>()
    override val incomingChannels: SharedFlow<AdnlChannel> = _newChannels.asSharedFlow()

    init {
        launch(CoroutineName("adnl-local-node-receive-loop")) {
            while (true) {
                val datagram = incomingDatagram.receive()
                processDatagram(datagram)
            }
        }
    }

    override fun createChannel(node: AdnlNode): AdnlChannel {
        channels.remove(node.shortId)?.close()
        val incoming = Channel<Pair<AdnlPacket, AdnlAddress>>()
        val peerPair = AdnlPeerPair(this, node, incoming, messageHandlers, queryHandlers)
        peerPair.coroutineContext.job.invokeOnCompletion {
            channels.remove(node.shortId)
            incoming.close(it)
        }
        channels[node.shortId] = incoming
        return peerPair
    }

    private suspend fun processDatagram(datagram: AdnlDatagram) {
        key.decryptIntoByteArray(datagram.packet, datagram.packet)
        val packet = TL.Boxed.decodeFromByteArray<AdnlPacket>(datagram.packet)
        val remoteId = packet.fromShort ?: packet.from?.shortId ?: return
        val channel = channels.getOrPut(remoteId) {
            val remoteNode = AdnlNode(packet.from ?: return, AdnlAddressList(datagram.address))
            val incoming = Channel<Pair<AdnlPacket, AdnlAddress>>()
            val peerPair = AdnlPeerPair(this, remoteNode, incoming)
            peerPair.coroutineContext.job.invokeOnCompletion {
                channels.remove(remoteId)
                incoming.close(it)
            }
            _newChannels.emit(peerPair)
            incoming
        }
        channel.send(packet to datagram.address)
    }
}

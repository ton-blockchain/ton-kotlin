package org.ton.kotlin.adnl

import io.ktor.network.sockets.*
import io.ktor.util.logging.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import kotlinx.io.bytestring.toHexString
import kotlinx.io.readByteArray
import kotlinx.io.readByteString
import kotlinx.io.write
import org.ton.kotlin.adnl.internal.AdnlDatagram
import org.ton.kotlin.adnl.internal.AdnlLocalNodeImpl
import org.ton.kotlin.adnl.util.Hash256Map
import org.ton.kotlin.crypto.PrivateKeyEd25519
import kotlin.coroutines.CoroutineContext

private val LOGGER = KtorSimpleLogger("org.ton.kotlin.adnl.Adnl")

public class Adnl(
    public val socket: BoundDatagramSocket,
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    private val localNodesMap = Hash256Map<AdnlIdShort, AdnlLocalNodeImpl> { it.hash }
    public var localNodes: Collection<AdnlLocalNode> = localNodesMap.values
        private set

    internal val channels = Hash256Map<AdnlIdShort, SendChannel<AdnlDatagram>> { it.hash }
    internal val nodeChannels = Hash256Map<AdnlIdShort, SendChannel<AdnlDatagram>> { it.hash }

    init {
        launch {
            for (datagram in socket.incoming) {
                processUdpDatagram(datagram)
            }
        }
    }

    public fun localNode(key: PrivateKeyEd25519, builder: AdnlLocalNodeBuilder.() -> Unit): AdnlLocalNode {
        val builder = AdnlLocalNodeBuilder()
        builder.builder()
        val channel = Channel<AdnlDatagram>()
        val publicKey = key.publicKey()
        val shortId = AdnlIdShort(publicKey.computeShortId())
        val newLocalNode = AdnlLocalNodeImpl(key, this, channel, builder.messageHandler, builder.queryHandler)
        localNodesMap[shortId] = newLocalNode
        nodeChannels[shortId] = channel
        localNodes = localNodesMap.values.toList()
        return newLocalNode
    }

    internal suspend fun sendDatagram(datagram: AdnlDatagram) {
        val packet = Buffer().apply {
            write(datagram.dest.hash)
            write(datagram.packet)
        }
        val address = datagram.address.toInetSocketAddress()
        val udpDatagram = Datagram(packet, address)
        socket.send(udpDatagram)
    }

    private suspend fun processUdpDatagram(datagram: Datagram) {
        if (datagram.packet.remaining < 32) {
            return
        }
        val id = AdnlIdShort(datagram.packet.readByteString(32))
        val address = datagram.address.toAdnlAddress()
        val packet = datagram.packet.readByteArray()
        val datagram = AdnlDatagram(id, address, packet)
        LOGGER.trace { "incoming datagram from $address to ${id.hash.toHexString()}" }

        val channel = channels[id]
        if (channel != null) {
            channel.send(datagram)
            return
        }

        val nodeChannel = nodeChannels[id]
        if (nodeChannel != null) {
            nodeChannel.send(datagram)
            return
        }
    }
}

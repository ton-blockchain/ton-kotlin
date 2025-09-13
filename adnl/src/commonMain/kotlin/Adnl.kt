package org.ton.kotlin.adnl

import io.ktor.network.sockets.*
import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.io.readByteArray
import kotlinx.io.readByteString
import kotlinx.serialization.decodeFromByteArray
import org.ton.kotlin.adnl.channel.AdnlChannel
import org.ton.kotlin.adnl.util.Hash256Map
import org.ton.kotlin.crypto.PrivateKeyEd25519
import org.ton.kotlin.tl.TL
import kotlin.coroutines.CoroutineContext

private val LOGGER = KtorSimpleLogger("org.ton.kotlin.adnl.Adnl")

class Adnl(
    val socket: BoundDatagramSocket,
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    private val localNodesMap = Hash256Map<ByteString, AdnlLocalNode> { it }
    var localNodes: Collection<AdnlLocalNode> = localNodesMap.values
        private set
    private val outDatagrams = Channel<Datagram>()

    init {
        launch {
            for (datagram in socket.incoming) {
                processDatagram(datagram.packet, datagram.address.toAdnlAddress())
            }
        }
    }

    fun localNode(key: PrivateKeyEd25519): AdnlLocalNode {
        val localNode = localNodesMap[key.computeShortId()]
        if (localNode != null) {
            return localNode
        }
        val newLocalNode = AdnlLocalNode(key, this)
        localNodesMap[key.computeShortId()] = newLocalNode
        localNodes = localNodesMap.values.toList()
        return newLocalNode
    }

    suspend fun sendDatagram(datagram: Buffer, address: AdnlAddress) {
        socket.send(Datagram(datagram, address.toInetSocketAddress()))
    }

    private suspend fun processDatagram(datagram: Source, address: AdnlAddress) {
        val id = AdnlIdShort(datagram.readByteString(32))
        LOGGER.trace { "incoming datagram from $address to ${id.hash.toHexString()}" }

        var localNode = localNodesMap[id.hash]
        var channel: AdnlChannel? = null
        var peerPair: AdnlPeerPair? = null
        if (localNode == null) {
            channel = localNodes.firstNotNullOfOrNull { localNode ->
                localNode.channel(id)
            }
            if (channel != null) {
                peerPair = channel.peerPair
                localNode = peerPair.localNode
            } else {
                println("Unknown dest: ?->$id")
                return
            }
        }

        val payload = datagram.readByteArray()
        val decryptor = channel?.decryptor ?: localNode.key.createDecryptor()
        val decrypted = decryptor.decryptToByteArray(payload)
        val packet = TL.Boxed.decodeFromByteArray<AdnlPacket>(decrypted)
        if (peerPair != null) {
            peerPair.processPacket(packet, checkSignature = false, address)
        } else {
            if (packet.from != null) {
                val node = AdnlNode(packet.from, packet.address ?: AdnlAddressList())
                peerPair = localNode.peer(node)
            } else if (packet.fromShort != null) {
                peerPair = localNode.peer(packet.fromShort)
            }
            if (peerPair == null) {
                println("Unknown src: ?->$id")
                return
            }
            peerPair.processPacket(packet, checkSignature = true, address)
        }
    }
}

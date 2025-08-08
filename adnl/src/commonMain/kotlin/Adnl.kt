package org.ton.kotlin.adnl

import io.ktor.network.sockets.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.io.readByteString
import kotlinx.serialization.decodeFromByteArray
import org.ton.kotlin.adnl.channel.AdnlChannel
import org.ton.kotlin.adnl.util.Hash256Map
import org.ton.kotlin.crypto.PrivateKeyEd25519
import org.ton.kotlin.tl.TL
import kotlin.coroutines.CoroutineContext

class Adnl(
    val socket: BoundDatagramSocket,
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    private val localNodesMap = Hash256Map<AdnlLocalNode>()
    var localNodes: Collection<AdnlLocalNode> = localNodesMap.values
        private set
    private val outDatagrams = Channel<Datagram>()

    init {
        launch {
            while (true) {
                select {
                    outDatagrams.onReceive {
                        socket.send(it)
                    }
                    socket.incoming.onReceive {
                        processDatagram(it.packet, it.address.toAdnlAddress())
                    }
                }
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
        outDatagrams.send(Datagram(datagram, address.toInetSocketAddress()))
    }

    private suspend fun processDatagram(datagram: Source, address: AdnlAddress) {
        val id = AdnlIdShort(datagram.readByteString(32))

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
            peerPair.processPacket(packet, checkSignature = false)
        } else {
            if (packet.from != null) {
                peerPair = localNode.peer(packet.from, address)
            } else if (packet.fromShort != null) {
                peerPair = localNode.peer(packet.fromShort)
            }
            if (peerPair == null) {
                println("Unknown src: ?->$id")
                return
            }
            peerPair.processPacket(packet, checkSignature = true)
        }
    }
}

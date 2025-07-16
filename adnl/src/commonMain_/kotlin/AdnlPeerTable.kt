package org.ton.kotlin.adnl

import io.ktor.network.sockets.BoundDatagramSocket
import io.ktor.network.sockets.Datagram
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.utils.io.core.remaining
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.io.readByteString
import org.ton.kotlin.adnl.channel.AdnlChannel
import org.ton.kotlin.adnl.node.AdnlLocalNode
import kotlin.coroutines.CoroutineContext

class AdnlPeerTable(
    val datagramSocket: BoundDatagramSocket
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main
    private val logger = KtorSimpleLogger("AdnlPeerTable")
    private val localNodes = ArrayList<AdnlLocalNode>()
    private val channels = ArrayList<AdnlChannel>()

    private suspend fun receiveAndProcessDatagram() {
        val datagram = datagramSocket.receive()
        val size = datagram.packet.remaining
        if (size < 32) {
            logger.warn("dropping IN message [?->]: message too short: $size bytes")
            return
        }

        val dst = AdnlIdShort(datagram.packet.readByteString(32))
        channels.find { it.inputId == dst }?.onDatagram(datagram) ?: localNodes.find { it.match(dst) }
            ?.onDatagram(datagram) ?: run {
            logger.warn("dropping IN message [?->?]: unknown destination: $dst")
        }
    }

    fun removeChannel(
        inputId: AdnlIdShort
    ) {

    }

    fun sendDatagram(it: Datagram) {
        TODO("Not yet implemented")
    }

    private val AdnlIdShort.localNode: AdnlLocalNode? get() = localNodes.find { it.match(this) }
}

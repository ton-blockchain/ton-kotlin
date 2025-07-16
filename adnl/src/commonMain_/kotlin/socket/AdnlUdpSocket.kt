package org.ton.kotlin.adnl.socket

import io.ktor.network.sockets.*
import io.ktor.util.logging.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.receiveAsFlow
import org.ton.kotlin.adnl.Adnl

class AdnlUdpSocket(
    val datagramSocket: BoundDatagramSocket
) : ASocket by datagramSocket, ABoundSocket by datagramSocket {
    private val logger: Logger = KtorSimpleLogger("AdnlUdpSocket")

    override fun dispose() {
        datagramSocket.dispose()
    }

    init {
        CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                try {
                    receiveAndProcessDatagram()
                } catch (e: CancellationException) {
                    // ignore
                } catch (cause: Exception) {
                    logger.error(cause)
                }
            }
        }
    }

    suspend fun send(datagram: Datagram) {
        datagramSocket.send(datagram)
    }

    private suspend fun receiveAndProcessDatagram() {
        val datagram = datagramSocket.receive()
        val size = datagram.packet.remaining
        if (size < 32) {
            logger.warn("received too small packet of size $size")
        }
    }
}

fun ASocket.adnl(adnl: Adnl) {
    AdnlUdpSocket(this as BoundDatagramSocket)
}

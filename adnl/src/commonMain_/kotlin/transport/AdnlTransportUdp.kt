package org.ton.kotlin.adnl.transport

import io.ktor.network.sockets.SocketAddress
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.io.Buffer
import org.ton.kotlin.adnl.socket.AdnlUdpSocket

class AdnlTransportUdp(
    val socket: AdnlUdpSocket
) : AdnlTransport<SocketAddress> {
    override val localAddress: SocketAddress get() = socket.localAddress

    override val incoming: ReceiveChannel<Buffer>
        get() = TODO()
    override val outgoing: SendChannel<Pair<Buffer, SocketAddress>>
        get() = TODO("Not yet implemented")


}

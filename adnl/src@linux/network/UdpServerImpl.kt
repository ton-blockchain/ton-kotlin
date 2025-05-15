package org.ton.kotlin.adnl.network

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

internal actual class UdpServerImpl actual constructor(
    coroutineContext: CoroutineContext,
    actual val port: Int,
    callback: UdpServer.Callback
) : UdpServer {
    override val coroutineContext: CoroutineContext = coroutineContext + CoroutineName(toString())
    private val socket = aSocket(SelectorManager(coroutineContext + CoroutineName("selector-$port")))
        .udp()
        .bind(localAddress = InetSocketAddress("0.0.0.0", port))
    private val job = launch(CoroutineName("listener-$port")) {
        while (true) {
            val datagram = socket.receive()
            val socketAddress = datagram.address as InetSocketAddress
            val address = socketAddress.hostname
            val port = socketAddress.port
            val data = datagram.packet
            try {
                launch {
                    callback.receive(IPAddress.ipv4(address, port), data)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun send(address: IPAddress, data: ByteReadPacket) {
        val datagram = Datagram(data, InetSocketAddress(address.host, port))
        socket.send(datagram)
    }
}

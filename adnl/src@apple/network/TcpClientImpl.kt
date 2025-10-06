package org.ton.adnl.network

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.newFixedThreadPoolContext

public actual class TcpClientImpl actual constructor(
) : TcpClient {

    private var isClosed by atomic(false)
    private lateinit var socket: Socket
    private lateinit var connection: Connection
    actual override val input: ByteReadChannel
        get() = connection.input
    actual override val output: ByteWriteChannel
        get() = connection.output

    actual override suspend fun connect(host: String, port: Int) {
        socket = aSocket(selectorManager).tcpNoDelay().tcp().connect(host, port)
        connection = socket.connection()
        isClosed = false
    }

    actual override fun close() {
        close(null)
    }

    actual override fun close(cause: Throwable?) {
        if (isClosed) return
        connection.input.cancel(cause)
        connection.output.close(cause)
        socket.close()
        isClosed = true
    }

    public companion object {
        private val tcpDispatcher = newFixedThreadPoolContext(2, "tcp")
        private val selectorManager = SelectorManager(tcpDispatcher)
    }
}

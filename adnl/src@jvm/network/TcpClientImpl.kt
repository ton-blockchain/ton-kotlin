package org.ton.kotlin.adnl.network

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
    override val input: ByteReadChannel
        get() = connection.input
    override val output: ByteWriteChannel
        get() = connection.output

    override suspend fun connect(host: String, port: Int) {
        socket = aSocket(selectorManager).tcpNoDelay().tcp().connect(host, port)
        connection = socket.connection()
        isClosed = false
    }

    override fun close() {
        close(null)
    }

    override fun close(cause: Throwable?) {
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

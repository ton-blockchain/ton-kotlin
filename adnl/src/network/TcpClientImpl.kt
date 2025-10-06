package org.ton.adnl.network

import io.ktor.utils.io.*

public expect class TcpClientImpl() : TcpClient {
    override val input: ByteReadChannel
    override val output: ByteWriteChannel
    override suspend fun connect(host: String, port: Int)
    override fun close(cause: Throwable?)
    override fun close()
}

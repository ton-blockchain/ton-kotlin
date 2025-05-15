package org.ton.kotlin.adnl.network

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.DisposableHandle

public interface TcpClient : Closeable, DisposableHandle {
    public val input: ByteReadChannel
    public val output: ByteWriteChannel

    public suspend fun connect(host: String, port: Int)

    override fun dispose() {
        try {
            close()
        } catch (ignore: Throwable) {
        }
    }

    public fun close(cause: Throwable?)
}

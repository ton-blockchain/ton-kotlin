@file:OptIn(ExperimentalForeignApi::class)

package org.ton.adnl.network

import io.ktor.utils.io.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
public actual class TcpClientImpl actual constructor(
) : TcpClient {
    actual override val input: ByteReadChannel
        get() = TODO("Not yet implemented")
    actual override val output: ByteWriteChannel
        get() = TODO("Not yet implemented")

    actual override suspend fun connect(host: String, port: Int) {
        TODO("Not yet implemented")
    }

    actual override fun close(cause: Throwable?) {
        TODO("Not yet implemented")
    }

    actual override fun close() {
        TODO("Not yet implemented")
    }
}

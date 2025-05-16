package org.ton.kotlin.adnl.network

import io.ktor.utils.io.core.*
import kotlin.coroutines.CoroutineContext

internal expect class UdpServerImpl(
    coroutineContext: CoroutineContext,
    port: Int,
    callback: UdpServer.Callback
) : UdpServer {
    val port: Int
    override suspend fun send(
        address: IPAddress,
        data: ByteReadPacket
    )

    override val coroutineContext: CoroutineContext
}

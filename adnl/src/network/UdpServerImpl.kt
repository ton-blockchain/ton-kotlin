package org.ton.kotlin.adnl.network

import kotlin.coroutines.CoroutineContext

internal expect class UdpServerImpl(
    coroutineContext: CoroutineContext,
    port: Int,
    callback: UdpServer.Callback
) : UdpServer {
    val port: Int
}

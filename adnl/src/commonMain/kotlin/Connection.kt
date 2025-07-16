package org.ton.kotlin.adnl

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.io.Buffer
import kotlin.coroutines.CoroutineContext


abstract class Connection(
    val host: Host,
    val node: AdnlNode,
    val isInitiator: Boolean,
    val transport: Transport,
    val localAddress: AddressList,
) : CoroutineScope {
    val remoteAddress: AddressList
        get() = node.addresses

    override val coroutineContext: CoroutineContext = host.coroutineContext + CoroutineName(toString())

    override fun toString(): String = "Connection[${host.localId}<->${node.peerId}]"

    protected abstract suspend fun sendDatagram(
        buffer: Buffer
    )
}

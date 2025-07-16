package org.ton.kotlin.adnl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

interface Network : CoroutineScope {
    val staticNodes: List<AdnlNode>
    val transports: List<Transport>

    suspend fun connect(
        host: Host,
        id: AdnlIdShort,
        addressList: Iterable<Address>
    ): Connection

    suspend fun disconnect(connection: Connection)
}

class NetworkImpl(
    override val transports: List<Transport>,
    val peerTable: PeerTable,
    override val staticNodes: List<AdnlNode> = emptyList()
) : Network {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    val connections = mutableListOf<Connection>()

    override suspend fun connect(
        host: Host,
        id: AdnlIdShort,
        addressList: Iterable<Address>
    ): Connection {
        peerTable.getInfo(host.localId, id) ?: staticNodes.firstOrNull { it.peerId == id }

        TODO("Not yet implemented")
    }

    override suspend fun disconnect(connection: Connection) {
        TODO("Not yet implemented")
    }


}

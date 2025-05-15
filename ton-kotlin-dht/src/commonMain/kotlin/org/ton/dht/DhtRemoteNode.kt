package org.ton.dht

import org.ton.kotlin.adnl.dht.DhtNode

class DhtRemoteNode(
    val dhtNode: DhtNode
) {
    val key get() = dhtNode.key()

}

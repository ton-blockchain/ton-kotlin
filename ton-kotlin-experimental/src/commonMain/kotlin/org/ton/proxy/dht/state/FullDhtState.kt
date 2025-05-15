package org.ton.proxy.dht.state

import org.ton.kotlin.adnl.adnl.AdnlIdShort
import org.ton.proxy.dht.DhtBucket
import org.ton.proxy.dht.DhtPeer
import org.ton.proxy.dht.storage.DhtStorage

class FullDhtState(
    localId: AdnlIdShort,
    knownPeers: MutableSet<DhtPeer>,
    override val storage: DhtStorage
) : AbstractDhtState(knownPeers) {
    override val buckets: DhtBucket = DhtBucket(localId)
}

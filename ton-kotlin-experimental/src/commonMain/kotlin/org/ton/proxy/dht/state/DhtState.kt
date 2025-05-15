package org.ton.proxy.dht.state

import org.ton.kotlin.adnl.adnl.AdnlIdShort
import org.ton.proxy.dht.DhtPeer
import org.ton.proxy.dht.storage.DhtStorage
import kotlin.jvm.JvmStatic

interface DhtState {
    val knownPeers: Set<DhtPeer>
    val badPeers: Map<AdnlIdShort, Int>
    fun addPeer(peer: DhtPeer): AdnlIdShort
    fun updatePeerStatus(peer: AdnlIdShort, isGood: Boolean)
    fun setGoodPeer(peer: AdnlIdShort)

    companion object {
        @JvmStatic
        fun full(
            localId: AdnlIdShort,
            knownPeers: Collection<DhtPeer>,
            storage: DhtStorage
        ) = FullDhtState(localId, knownPeers.toMutableSet(), storage)

        @JvmStatic
        fun lite(
            knownPeers: Collection<DhtPeer>
        ) = LiteDhtState(knownPeers.toMutableSet())
    }
}

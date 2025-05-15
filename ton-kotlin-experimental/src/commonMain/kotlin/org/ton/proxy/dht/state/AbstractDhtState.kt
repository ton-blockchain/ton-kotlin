package org.ton.proxy.dht.state

import io.ktor.util.collections.*
import org.ton.kotlin.adnl.adnl.AdnlIdShort
import org.ton.logger.Logger
import org.ton.logger.PrintLnLogger
import org.ton.proxy.dht.DhtBucket
import org.ton.proxy.dht.DhtPeer
import org.ton.proxy.dht.storage.DhtStorage

abstract class AbstractDhtState(
    override val knownPeers: MutableSet<DhtPeer>
) : DhtState {
    abstract val buckets: DhtBucket?
    abstract val storage: DhtStorage?
    override val badPeers = ConcurrentMap<AdnlIdShort, Int>()

    private val logger = PrintLnLogger("DhtState", Logger.Level.DEBUG)

    override fun addPeer(peer: DhtPeer): AdnlIdShort {
        val peerKey = peer.key
        val peerId = peerKey.toAdnlIdShort()

        if (knownPeers.add(peer)) {
            logger.debug { "Add new peer: $peer" }
            buckets?.set(peerId, peer.dhtNode)
        } else {
            setGoodPeer(peerId)
        }

        return peerId
    }

    override fun updatePeerStatus(peer: AdnlIdShort, isGood: Boolean) {
        if (isGood) {
            setGoodPeer(peer)
        } else {
            val value = badPeers[peer]
            if (value == null) {
                badPeers[peer] = 0
            } else {
                badPeers[peer] = value + 2
            }
            logger.debug { "Bad peer: ${badPeers[peer]}" }
        }
    }

    override fun setGoodPeer(peer: AdnlIdShort) {
        logger.debug { "Good peer: $peer" }
        val value = badPeers[peer] ?: return
        if (value <= 1) {
            badPeers.remove(peer)
        } else {
            badPeers[peer] = value - 1
        }
    }
}

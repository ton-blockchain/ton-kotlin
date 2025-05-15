package org.ton.proxy.dht

import org.ton.kotlin.adnl.adnl.AdnlIdShort
import org.ton.kotlin.adnl.dht.DhtNode
import org.ton.kotlin.adnl.dht.DhtNodes
import org.ton.proxy.dht.Dht.Companion.AFFINITY_BITS
import org.ton.proxy.dht.Dht.Companion.affinity
import kotlin.experimental.xor

class DhtBucket(
    val localId: AdnlIdShort,
    buckets: Array<MutableMap<AdnlIdShort, DhtNode>> = Array(256) { HashMap() }
) : Iterable<Map<AdnlIdShort, DhtNode>> {
    private val _buckets = buckets
    val buckets: List<Map<AdnlIdShort, DhtNode>> get() = _buckets.toList()

    override fun iterator(): Iterator<Map<AdnlIdShort, DhtNode>> = _buckets.iterator()

    operator fun set(peerId: AdnlIdShort, dhtNode: DhtNode) {
        val affinity = affinity(localId.id, peerId.id)
        val result = _buckets[affinity][peerId]
        if (result != null) {
            if (result.version < dhtNode.version) {
                _buckets[affinity][peerId] = dhtNode
            }
        } else {
            _buckets[affinity][peerId] = dhtNode
        }
    }

    operator fun get(peerId: AdnlIdShort, k: Int): DhtNodes {
        val key1 = localId.id
        val key2 = peerId.id

        var distance = 0
        val nodes = ArrayList<DhtNode>()

        // Iterate over buckets
        for (i in 0 until 32) {
            var subDistance = distance

            // Compare bytes
            var xor = (key1[i] xor key2[i]).toUInt().toInt()

            // If they are not equal (otherwise we will just add 8 bits
            // to the distance and continue to the next byte)
            while (xor != 0) {
                if (xor and 0xF0 == 0) {
                    // If high 4 bits of the comparison result are equal then shift comparison
                    // result to the left, so that low 4 bits will become the high 4 bits
                    subDistance += 4
                    xor = xor shl 4
                } else {
                    // Get equal bits count
                    val shift = AFFINITY_BITS[xor shr 4]
                    subDistance += shift

                    // Add nodes from the bucket
                    val bucket = _buckets[subDistance]
                    for (value in bucket.values) {
                        nodes.add(value)
                        if (nodes.size >= k) {
                            break
                        }
                    }

                    // Skip one different bit:
                    // xor = 0000____ | shift + 1 = 5, xor = ________
                    // xor = 0001____ | shift + 1 = 4, xor = ________
                    // xor = 001x____ | shift + 1 = 3, xor = x_______
                    // xor = 01xx____ | shift + 1 = 2, xor = xx______
                    // xor = 1xxx____ | shift + 1 = 1, xor = xxx_____
                    xor = xor shl (shift + 1)
                    subDistance -= 1
                }
            }

            // Increase distance
            distance += 8
        }

        return DhtNodes(nodes)
    }
}

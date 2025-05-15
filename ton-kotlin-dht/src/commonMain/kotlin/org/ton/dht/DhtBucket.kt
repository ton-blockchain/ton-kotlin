package org.ton.dht

import org.ton.kotlin.adnl.dht.DhtNode
import org.ton.kotlin.bitstring.BitString

class DhtBucket(
    k: Int
) {
    private val activeNodes = ArrayList<DhtRemoteNode>(k)
    private val backupNodes = ArrayList<DhtRemoteNode>(k)

    public val activeCount get() = activeNodes.size

    public fun getNearestNode(
        id: Bits256,
        k: Int
    ): List<DhtNode> {
        if (activeNodes.size == 0) return emptyList()

        val map = HashMap<Bits256, Int>()
        for (i in activeNodes.indices) {
            val node = activeNodes[i]
            val distance = id xor node.key.id
            map[distance] = i
        }

        return map.entries.asSequence()
            .sortedBy { it.key }
            .map { activeNodes[it.value].dhtNode }
            .take(k)
            .toList()
    }

    public companion object {
        public const val PING_TIMEOUT_MS = 60_000L
        public const val MAX_MISSED_PINGS = 3L
    }
}

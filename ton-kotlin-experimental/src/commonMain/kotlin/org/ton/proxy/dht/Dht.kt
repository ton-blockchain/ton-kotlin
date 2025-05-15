package org.ton.proxy.dht

import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.ton.kotlin.adnl.adnl.AdnlAddressList
import org.ton.kotlin.adnl.adnl.AdnlIdShort
import org.ton.kotlin.adnl.dht.DhtKey
import org.ton.kotlin.adnl.dht.DhtNode
import org.ton.kotlin.adnl.dht.DhtValue
import org.ton.kotlin.adnl.dht.DhtValueFound
import org.ton.kotlin.adnl.pub.PublicKey
import org.ton.kotlin.bitstring.BitString
import org.ton.proxy.adnl.resolver.AdnlAddressResolver
import org.ton.proxy.dht.state.DhtState
import org.ton.proxy.dht.storage.DhtStorage
import kotlin.experimental.and
import kotlin.experimental.xor
import kotlin.jvm.JvmStatic
import kotlin.time.Duration.Companion.minutes

class Dht(
    val state: DhtState,
    val config: DhtConfig = DhtConfig()
) : AdnlAddressResolver {
    private val cache = Cache.Builder()
        .expireAfterWrite(60.minutes)
        .build<BitString, DhtValue>()

    suspend fun findValue(key: DhtKey): DhtValue? = findValue(key.key())

    suspend fun findValue(key: ByteArray): DhtValue? {
        val cachedKey = BitString(key)
        cache.get(cachedKey)?.let { return it }
        val knownPeers = state.knownPeers
            .filter { !state.badPeers.containsKey(it.dhtNode.id.toAdnlIdShort()) }
            .sortedByDescending {
                affinity(key, it.dhtNode.id.toAdnlIdShort().id)
            }
        return DhtValuesFlow(key, knownPeers).map { (peer, result) ->
            if (result == null) {
                state.updatePeerStatus(peer.dhtNode.id.toAdnlIdShort(), isGood = false)
            } else {
                state.addPeer(peer)
            }
            result
        }.filterIsInstance<DhtValueFound>().firstOrNull()?.value?.also {
            cache.put(cachedKey, it)
        }
    }

    override suspend fun resolve(address: AdnlIdShort): Pair<PublicKey, AdnlAddressList>? {
        val value = findValue(DhtKey.address(address)) ?: return null
        return value.key.id to AdnlAddressList.decodeBoxed(value.value)
    }

    companion object {
        internal val AFFINITY_BITS = intArrayOf(4, 3, 2, 2, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0)

        @JvmStatic
        fun full(
            localId: AdnlIdShort,
            knownPeers: Collection<DhtNode>,
            storage: DhtStorage
        ) = Dht(DhtState.full(localId, knownPeers.asPeers(), storage))

        @JvmStatic
        fun lite(
            knownPeers: Collection<DhtNode>
        ) = Dht(DhtState.lite(knownPeers.asPeers()))

        @JvmStatic
        fun affinity(key1: ByteArray, key2: ByteArray): Int {
            var result = 0
            for (i in 0 until 32) {
                val x = key1[i] xor key2[i]
                result += if (x == 0.toByte()) {
                    8
                } else {
                    if (x and 0xF0.toByte() == 0.toByte()) {
                        AFFINITY_BITS[(x.toUByte() and 0x0Fu).toInt()] + 4
                    } else {
                        AFFINITY_BITS[(x.toUByte().toInt() shr 4)]
                    }
                }
            }
            return result
        }

        private fun Collection<DhtNode>.asPeers() = asSequence()
            .distinctBy { it.id }
            .mapNotNull { DhtPeer(it) }
            .toList()
    }
}

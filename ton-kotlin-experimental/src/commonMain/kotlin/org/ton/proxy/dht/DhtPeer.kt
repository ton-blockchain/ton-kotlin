package org.ton.proxy.dht

import kotlinx.datetime.Clock
import org.ton.kotlin.adnl.ipv4
import org.ton.kotlin.adnl.adnl.AdnlAddress
import org.ton.kotlin.adnl.adnl.AdnlAddressList
import org.ton.kotlin.adnl.adnl.AdnlAddressUdp
import org.ton.kotlin.adnl.adnl.AdnlIdShort
import org.ton.kotlin.adnl.dht.*
import org.ton.kotlin.adnl.dht.functions.DhtFindValue
import org.ton.kotlin.adnl.dht.functions.DhtPing
import org.ton.proxy.adnl.AdnlPeer
import kotlin.jvm.JvmStatic

class DhtPeer private constructor(
    val dhtNode: DhtNode,
    address: AdnlAddressUdp
) : AdnlPeer(address, dhtNode.id) {
    /**
     * `dht.ping random_id:long = dht.Pong;`
     */
    suspend fun ping(long: Long = Clock.System.now().epochSeconds): DhtPong = query(DhtPing(long))

    /**
     * `dht.findValue key:int256 k:int = dht.ValueResult;`
     */
    suspend fun findValue(key: ByteArray, k: Int = 6): DhtValueResult = query(DhtFindValue(key, k))
    suspend fun findValue(key: DhtKey, k: Int = 6): DhtValueResult = query(DhtFindValue(key, k))

    suspend fun findAddress(adnlIdShort: AdnlIdShort): Pair<AdnlAddressList?, DhtNodes?> {
        return when (val result = findValue(DhtKey.address(adnlIdShort), 6)) {
            is DhtValueFound -> AdnlAddressList.decodeBoxed(result.value.value) to null
            is DhtValueNotFound -> null to result.nodes
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DhtPeer) return false
        if (dhtNode != other.dhtNode) return false
        return true
    }

    override fun hashCode(): Int = dhtNode.hashCode()

    override fun toString(): String = "[${dhtNode.id.toAdnlIdShort()}, ${ipv4(address.ip)}:${address.port}]"

    companion object {
        @JvmStatic
        fun from(dhtNode: DhtNode): DhtPeer? {
            val address = dhtNode.addr_list.addrs.filterIsInstance<AdnlAddressUdp>().firstOrNull() ?: return null
            return DhtPeer(dhtNode, address)
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun DhtPeer(dhtNode: DhtNode): DhtPeer? = DhtPeer.from(dhtNode)

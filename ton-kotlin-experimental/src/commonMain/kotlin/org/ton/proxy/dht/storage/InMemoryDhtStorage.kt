package org.ton.proxy.dht.storage

import kotlinx.datetime.Clock
import org.ton.kotlin.adnl.dht.DhtValue
import org.ton.kotlin.bitstring.BitString

class InMemoryDhtStorage(
    config: DhtStorageConfig,
    val map: MutableMap<BitString, DhtValue> = HashMap()
) : AbstractDhtStorage(config) {
    override fun putUnchecked(key: BitString, value: DhtValue) {
        map[key] = value
    }

    override fun getUnchecked(key: BitString): DhtValue? {
        return map[key]
    }

    override fun gc() {
        val now = Clock.System.now()
        map.entries.removeAll { it.value.ttl() <= now }
    }
}

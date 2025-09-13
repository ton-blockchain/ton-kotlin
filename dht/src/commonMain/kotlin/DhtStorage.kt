package org.ton.kotlin.dht

import org.ton.kotlin.adnl.util.Hash256Map
import org.ton.kotlin.dht.bucket.Key

interface DhtStorage {
    suspend fun findValue(key: Key): DhtValue?

    suspend fun storeValue(value: DhtValue)
}

class MemoryDhtStorage(
    val values: MutableMap<Key, DhtValue> = Hash256Map({ it.hash }),
) : DhtStorage {
    override suspend fun findValue(key: Key): DhtValue? {
        return values[key]
    }

    override suspend fun storeValue(value: DhtValue) {
        values[value.key] = value
    }
}

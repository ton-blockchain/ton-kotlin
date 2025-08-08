package org.ton.kotlin.dht

import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.adnl.util.Hash256Map
import org.ton.kotlin.dht.bucket.Key

interface DhtStorage {
    suspend fun findValue(key: Key): DhtValue?

    suspend fun storeValue(value: DhtValue)
}

class MemoryDhtStorage(
    val values: MutableMap<ByteString, DhtValue> = Hash256Map(),
) : DhtStorage {
    override suspend fun findValue(key: Key): DhtValue? {
        return values[key.hash]
    }

    override suspend fun storeValue(value: DhtValue) {
        values[value.key.key.keyId.hash] = value
    }
}

package org.ton.proxy.dht.storage

import org.ton.kotlin.adnl.dht.DhtValue
import org.ton.kotlin.bitstring.BitString

interface DhtStorage {
    val config: DhtStorageConfig

    fun gc()

    fun put(value: DhtValue): Boolean
    fun get(bitString: BitString): DhtValue?
}

package org.ton.proxy.dht

import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Duration.Companion.milliseconds
import org.ton.kotlin.adnl.dht.DhtKey
import org.ton.kotlin.adnl.dht.DhtStore

@Serializable
data class DhtConfig(
    /**
     * Default stored value timeout used for [DhtStore]
     */
    val valueTtl: Duration = 3600.seconds,

    /**
     * ADNL query timeout
     */
    val queryTimeout: Duration = 1000.milliseconds,

    /**
     * Amount of DHT peers, used for values search
     */
    val defaultValueBatchLength: Int = 5,

    /**
     * Max peer penalty points. On each unsuccessful query every peer gains 2 points,
     * and then they are reduced by one on each good action.
     */
    val badPeerThreshold: Int = 5,

    /**
     * Max allowed `k` value for DHT `FindValue` query.
     */
    val maxAllowedK: Int = 5,

    /**
     * Max allowed key name length (in bytes).
     * @see [DhtKey.name]
     */
    val maxKeyNameLength: Int = 15,

    /**
     * Max allowed key index
     * @see [DhtKey.idx]
     */
    val maxKeyIndex: Int = 15,

    /**
     * Storage GC interval. Will remove all outdated entries
     */
    val storageGcInterval: Duration = 10.seconds
)

package org.ton.kotlin.dht.bucket

import org.ton.kotlin.dht.K_VALUE
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class KBucketConfig(
    /**
     * Maximal number of nodes that a bucket can contain.
     */
    val bucketSize: Int = K_VALUE,

    /**
     *
     */
    val pendingTimeout: Duration = 60.seconds
)

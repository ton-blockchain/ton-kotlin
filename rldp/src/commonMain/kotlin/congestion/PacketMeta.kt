@file:OptIn(ExperimentalTime::class)

package org.ton.kotlin.rldp.congestion

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

internal data class PacketMeta(
    val seqno: Int,
    val sentTime: Instant
)

@file:Suppress("NOTHING_TO_INLINE")

package org.ton.kotlin.block

import kotlinx.serialization.SerialName

@SerialName("gas_limits")

public data class VmGasLimits(
    val remaining: Long,
    val max_limit: Long,
    val cur_limit: Long,
    val credit: Long
)

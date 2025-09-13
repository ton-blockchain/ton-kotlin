package org.ton.kotlin.overlay

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class OverlayOptions(
    val announceSelf: Boolean = true,
    val reconnectBackoff: Duration = 5.seconds,
    val requestTimeout: Duration = 3.seconds,
    val pingInterval: Duration = 7.seconds,
    val maxNeighbours: Int = 16,
    val targetFastNeighbours: Int = 3,
    val targetMidNeighbours: Int = 3,
    val targetSlowNeighbours: Int = 3,
)

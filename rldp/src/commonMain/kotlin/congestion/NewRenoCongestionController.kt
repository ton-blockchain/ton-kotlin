package org.ton.kotlin.rldp.congestion

import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal class NewRenoCongestionController(
    val rttStats: RttStats,
    val lossReductionFactor: Double = 0.5,
    val minimumWindowSize: Int = 2,
    val maximumWindowSize: Int = 1000
) : AbstractCongestionController() {
    init {
        require(minimumWindowSize > 0) { "minimumWindowSize must be positive" }
        require(maximumWindowSize >= minimumWindowSize) { "maximumWindowSize must be greater than or equal to minimumWindowSize" }
    }

    var slowStartThreshold = Int.MAX_VALUE
        private set

    private var ackedSinceLastWindowIncrease = 0

    val isSlowStart: Boolean
        get() = congestionWindow < slowStartThreshold

    override fun pacingDelay(): Duration = rttStats.smoothedRtt / congestionWindow.coerceAtLeast(minimumWindowSize)

    override fun onConfirm(ackSymbols: Collection<PacketMeta>, now: Instant) {
        super.onConfirm(ackSymbols, now)

        if (isSlowStart) {
            congestionWindow += ackSymbols.size
        } else {
            val increase = ackSymbols.size.toDouble() / congestionWindow
            congestionWindow += increase.roundToInt().coerceAtLeast(1)
        }

        congestionWindow = congestionWindow.coerceAtMost(maximumWindowSize)
    }

    override fun onLost(lostSymbols: Collection<PacketMeta>, now: Instant) {
        super.onLost(lostSymbols, now)
        slowStartThreshold = (congestionWindow * lossReductionFactor).roundToInt().coerceAtLeast(minimumWindowSize)
        congestionWindow = slowStartThreshold
        ackedSinceLastWindowIncrease = 0
    }
}

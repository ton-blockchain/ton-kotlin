@file:OptIn(ExperimentalTime::class)

package org.ton.kotlin.rldp.congestion

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val RTT_WINDOW = 300.seconds

/**
 * Collector for Round-Trip Time (RTT) statistics,
 * using Exponentially Weighted Moving Average (EWMA) and a windowed minimum.
 */
internal class RttStats(
    initialRtt: Duration = Duration.ZERO,
) {
    var rtt = initialRtt
        private set
    var latestRtt = Duration.ZERO
        private set
    var rttVar = initialRtt / 2
        private set

    private var hasFirstRttSample = false
    private val rttSamples = MinMax(initialRtt.inWholeNanoseconds)

    val minRtt get() = rttSamples.value.nanoseconds
    private var maxRtt = initialRtt

    fun onRttSample(
        rttSample: Duration,
        now: Instant = Clock.System.now()
    ) {
        onRttSample(
            rttSample,
            Duration.ZERO,
            now
        )
    }

    /**
     * Record a new RTT sample.
     * @param rttSample RTT sample as Duration
     * @param ackDelay Delay reported by receiver as Duration
     * @param now Optional timestamp for this sample (for tests)
     */
    fun onRttSample(
        rttSample: Duration,
        ackDelay: Duration = Duration.ZERO,
        now: Instant = Clock.System.now()
    ) {
        latestRtt = rttSample
        if (!hasFirstRttSample) {
            rttSamples.reset(now, latestRtt.inWholeNanoseconds)
            rtt = latestRtt
            maxRtt = latestRtt
            rttVar = latestRtt / 2
            hasFirstRttSample = true
            return
        }

        rttSamples.runningMin(RTT_WINDOW, now, latestRtt.inWholeNanoseconds)
        maxRtt = maxRtt.coerceAtLeast(latestRtt)

        var adjustedRtt = latestRtt
        if (latestRtt >= rttSamples.value.nanoseconds + ackDelay) {
            adjustedRtt = latestRtt - ackDelay
        }
        rttVar = rttVar * 3 / 4 + ((rtt - adjustedRtt).absoluteValue / 4)
        rtt = rtt * 7 / 8 + (adjustedRtt / 8)
    }
}

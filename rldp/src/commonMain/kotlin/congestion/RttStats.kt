@file:OptIn(ExperimentalTime::class)

package org.ton.kotlin.rldp.congestion

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Collector for Round-Trip Time (RTT) statistics,
 * using Exponentially Weighted Moving Average (EWMA) and a windowed minimum.
 *
 * @param windowDuration Duration of the sliding window to compute minimum RTT.
 * @param now Initial timestamp reference for windowed statistics.
 */
internal class RttStats(
    private val windowDuration: Duration = 5.seconds,
    now: Instant = Clock.System.now()
) {
    /** The minimum RTT observed over the entire session. */
    var minRtt: Duration = Duration.INFINITE

    /** The minimum RTT observed within the sliding window. */
    var windowedMinRtt: Duration = Duration.INFINITE

    /** The most recent RTT sample. */
    var lastRtt: Duration = Duration.ZERO

    /** EWMA-smoothed RTT. */
    var smoothedRtt: Duration = Duration.ZERO

    /** EWMA-estimated RTT variance. */
    var rttVar: Duration = Duration.ZERO

    /** The number of RTT rounds since the start. */
    var rttRound: Long = 0

    private var rttRoundAt: Instant = now
    private val timedMin = TimedMinStat(windowDuration, now)

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
        // ignore outliers <1ms or >10s
        if (rttSample < 1.milliseconds || rttSample > 10.seconds) return
        if (ackDelay < Duration.ZERO) return
        lastRtt = rttSample

        // update sliding-window min RTT
        val sampleMs = rttSample.inWholeMilliseconds.toDouble()
        timedMin.addSample(sampleMs, now)
        val windowMinMs = timedMin.getMin(now)
        windowedMinRtt = windowMinMs.milliseconds

        // initialize or update EWMA and variance
        if (smoothedRtt == Duration.ZERO) {
            minRtt = rttSample
            smoothedRtt = rttSample
            rttVar = rttSample / 2
        } else {
            if (rttSample < minRtt) minRtt = rttSample
            var adjusted = rttSample
            if (adjusted - ackDelay > minRtt) adjusted -= ackDelay
            // EWMA alpha = 1/8
            smoothedRtt += (adjusted - smoothedRtt) / 8
            // variance EWMA beta = 1/4
            val diff = (smoothedRtt - adjusted).absoluteValue
            rttVar += (diff - rttVar) / 4
        }

        // update RTT rounds
        if (now - rttRoundAt >= windowDuration) {
            rttRoundAt = now
            rttRound++
        }
    }

    private class TimedMinStat(
        private val duration: Duration,
        now: Instant
    ) {
        private var currentMin: Double = Double.MAX_VALUE
        private var nextMin: Double = Double.MAX_VALUE
        private var currentTimestamp: Instant = now
        private var nextTimestamp: Instant = now

        /**
         * Add a sample at time `now`.
         */
        fun addSample(sample: Double, now: Instant) {
            update(now)
            if (sample < currentMin) currentMin = sample
            if (sample < nextMin) nextMin = sample
        }

        /**
         * Return the minimum over the current window.
         */
        fun getMin(now: Instant): Double {
            update(now)
            return if (currentMin.isFinite()) currentMin else Double.MAX_VALUE
        }

        // Slide window based on timestamp progression
        private fun update(now: Instant) {
            if (duration <= Duration.ZERO) return
            // if too far ahead, reset both
            if (now >= nextTimestamp + duration * 2) {
                currentMin = Double.MAX_VALUE
                currentTimestamp = now
                nextMin = Double.MAX_VALUE
                nextTimestamp = now
            } else if (now >= nextTimestamp + duration) {
                // shift next to current
                currentMin = nextMin
                currentTimestamp = nextTimestamp
                nextMin = Double.MAX_VALUE
                nextTimestamp = now
            }
        }
    }
}

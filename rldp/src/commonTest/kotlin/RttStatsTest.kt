@file:OptIn(ExperimentalTime::class)

import org.ton.kotlin.rldp.congestion.RttStats
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

class RttStatsTest {
    @Test
    fun testRttStatsAccumulatesValues() {
        val now = Clock.System.now()
        val stats = RttStats(now = now)
        assertEquals(Duration.ZERO, stats.smoothedRtt)

        stats.onRttSample((-1).milliseconds, Duration.ZERO)
        assertEquals(Duration.ZERO, stats.smoothedRtt)
        stats.onRttSample(1.milliseconds, (-1).milliseconds)
        assertEquals(Duration.ZERO, stats.smoothedRtt)

        stats.onRttSample(1.milliseconds, Duration.ZERO, now)
        stats.onRttSample(2.milliseconds, Duration.ZERO, now)
        stats.onRttSample(1.milliseconds, Duration.ZERO, now)
        stats.onRttSample(2.milliseconds, Duration.ZERO, now)
        stats.onRttSample(1.milliseconds, Duration.ZERO, now)
        stats.onRttSample(2.milliseconds, Duration.ZERO, now)

        assertEquals(2.milliseconds, stats.lastRtt)
        assertEquals(1.milliseconds, stats.minRtt)
        assertTrue(stats.smoothedRtt > 1.milliseconds && stats.smoothedRtt < 2.milliseconds)
        assertTrue(stats.rttVar > 0.1.milliseconds && stats.rttVar < 0.9.milliseconds)
    }

    @Test
    fun testWindowedMinRttSliding() {
        var now = Clock.System.now()
        // set windowDuration to 2ms for fast slide
        val stats = RttStats(windowDuration = 2.milliseconds, now)
        stats.onRttSample(5.milliseconds, Duration.ZERO, now)
        assertEquals(5.milliseconds, stats.windowedMinRtt)
        now += 1.milliseconds
        stats.onRttSample(3.milliseconds, Duration.ZERO, now)
        assertEquals(3.milliseconds, stats.windowedMinRtt)
        now += 2.milliseconds
        stats.onRttSample(4.milliseconds, Duration.ZERO, now)
        // first sample expired (older than 2ms), windowed min should be min of remaining (3) and new (4)
        assertEquals(3.milliseconds, stats.windowedMinRtt)
    }

    @Test
    fun testRttRoundIncrement() {
        var now = Clock.System.now()
        // windowDuration of 2ms
        val stats = RttStats(windowDuration = 2.milliseconds, now)
        stats.onRttSample(1.milliseconds, Duration.ZERO, now)
        assertEquals(0, stats.rttRound)
        now += 3.milliseconds
        stats.onRttSample(2.milliseconds, Duration.ZERO, now)
        assertEquals(1, stats.rttRound)
    }

    @Test
    fun testAckDelayAdjustment() {
        val now = Clock.System.now()
        val stats = RttStats(now = now)
        // initial sample to set minRtt and smoothedRtt
        stats.onRttSample(10.milliseconds, Duration.ZERO, now)
        // sample larger with ackDelay: adjusted = 15 - 5 = 10
        stats.onRttSample(15.milliseconds, 5.milliseconds, now)
        // smoothedRtt += (adjusted - smoothedRtt) / 8 = (15 - 10) / 8 = 0.625ms → 10.625ms
        assertEquals(10.625.milliseconds, stats.smoothedRtt)
        assertTrue(stats.rttVar > 4.milliseconds && stats.rttVar < 5.milliseconds)
    }
}

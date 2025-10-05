package org.ton.kotlin.rldp.congestion

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class MinMax(
    value: Long
) {
    private val estimateTime = Array(3) { Clock.System.now() }
    private val estimateValue = LongArray(3) { value }

    val value get() = estimateValue[0]

    /**
     * Resets the estimates to the given value.
     */
    fun reset(time: Instant, meas: Long): Long {
        estimateTime.fill(time)
        estimateValue.fill(meas)
        return meas
    }

    /**
     * Updates the min estimate based on the given measurement, and returns it.
     */
    fun runningMin(win: Duration, time: Instant, meas: Long): Long {
        val deltaTIme = time - estimateTime[2]
        if (meas <= estimateValue[0] || deltaTIme > win) {
            return reset(time, meas)
        }

        if (meas <= estimateValue[1]) {
            estimateValue[2] = estimateValue[1]
            estimateTime[2] = estimateTime[1]
            estimateValue[1] = meas
            estimateTime[1] = time
        } else if (meas <= estimateValue[2]) {
            estimateValue[2] = meas
            estimateTime[2] = time
        }

        return subwinUpdate(win, time, meas)
    }

    /**
     * Updates the max estimate based on the given measurement, and returns it.
     */
    fun runningMax(win: Duration, time: Instant, meas: Long): Long {
        val deltaTime = time - estimateTime[2]

        if (meas >= estimateValue[0] || deltaTime > win) {
            return reset(time, meas)
        }

        if (meas >= estimateValue[1]) {
            estimateValue[2] = estimateValue[1]
            estimateTime[2] = estimateTime[1]
            estimateValue[1] = meas
            estimateTime[1] = time
        } else if (meas >= estimateValue[2]) {
            estimateValue[2] = meas
            estimateTime[2] = time
        }

        return subwinUpdate(win, time, meas)
    }

    // As time advances, update the 1st, 2nd and 3rd estimates.
    private fun subwinUpdate(win: Duration, time: Instant, meas: Long): Long {
        val deltaTime = time - estimateTime[0]

        if (deltaTime > win) {
            // Passed entire window without a new val so make 2nd estimate the
            // new val & 3rd estimate the new 2nd choice. we may have to iterate
            // this since our 2nd estimate may also be outside the window (we
            // checked on entry that the third estimate was in the window).
            estimateValue[0] = estimateValue[1]
            estimateTime[0] = estimateTime[1]
            estimateValue[1] = estimateValue[2]
            estimateTime[1] = estimateTime[2]
            estimateValue[2] = meas
            estimateTime[2] = time

            if (time - estimateTime[0] > win) {
                estimateValue[0] = estimateValue[1]
                estimateTime[0] = estimateTime[1]
                estimateValue[1] = estimateValue[2]
                estimateTime[1] = estimateTime[2]
                estimateValue[2] = meas
                estimateTime[2] = time
            }
        } else if (estimateTime[1] == estimateTime[0] && deltaTime > win / 4.0) {
            // We've passed a quarter of the window without a new val so take a
            // 2nd estimate from the 2nd quarter of the window.
            estimateValue[2] = meas
            estimateTime[2] = time
            estimateValue[1] = meas
            estimateTime[1] = time
        } else if (estimateTime[2] == estimateTime[1] && deltaTime > win / 2.0) {
            // We've passed half the window without finding a new val so take a
            // 3rd estimate from the last half of the window.
            estimateValue[2] = meas
            estimateTime[2] = time
        }

        return estimateValue[0]
    }
}

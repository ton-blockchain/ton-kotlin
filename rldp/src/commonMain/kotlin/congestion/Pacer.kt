package org.ton.kotlin.rldp.congestion

import kotlin.math.min
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class Pacer(
    capacity: Long,
    rate: Long,
    private val maxDatagramSize: Int,
    private val maxPacingRate: Long = -1
) {
    var capacity = capacity / maxDatagramSize * maxDatagramSize
        private set

    var rate = if (maxPacingRate > 0) min(maxPacingRate, rate) else rate
        private set

    var nextTime = Clock.System.now()
        private set

    private var interval = Duration.ZERO
    private var lastUpdate = Clock.System.now()
    private var used = 0L
    private var lastPacketSize = -1

    fun update(capacity: Long, rate: Long, now: Instant) {
        val c = capacity / maxDatagramSize * maxDatagramSize
        if (this.capacity != c) {
            reset(now)
        }
        this.capacity = capacity
        this.rate = if (maxPacingRate > 0) min(maxPacingRate, rate) else rate
    }

    fun reset(now: Instant) {
        used = 0
        lastUpdate = now
        nextTime = nextTime.coerceAtMost(now)
        lastPacketSize = -1
        interval = Duration.ZERO
    }

    fun send(packetSize: Int, now: Instant) {
        if (rate == 0L) {
            return reset(now)
        }
        if (interval.isPositive()) {
            nextTime = nextTime.coerceAtMost(now) + interval
            interval = Duration.ZERO
        }

        val iv = (capacity.toDouble() / rate.toDouble()).seconds
        val elapsed = now - lastUpdate
        if (elapsed > iv) {
            reset(now)
        }

        used += packetSize

        val sameSize = if (lastPacketSize > 0) lastPacketSize == packetSize else true
        lastPacketSize = packetSize

        if (used >= capacity || !sameSize) {
            interval = (used.toDouble() / rate.toDouble()).seconds
            used = 0
            lastUpdate = now
            lastPacketSize = -1
        }
    }
}

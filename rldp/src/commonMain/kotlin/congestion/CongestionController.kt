@file:OptIn(ExperimentalTime::class)

package org.ton.kotlin.rldp.congestion

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

internal interface CongestionController {
    val inFlight: Int

    val congestionWindow: Int

    fun canSend(count: Int = 1): Boolean = (inFlight + count) < congestionWindow

    fun pacingDelay(): Duration

    fun onConfirm(
        ackSymbols: Collection<PacketMeta>,
        now: Instant = Clock.System.now(),
    )

    fun onSent(
        seqno: Int,
        now: Instant = Clock.System.now()
    ): PacketMeta

    fun onLost(
        lostSymbols: Collection<PacketMeta>,
        now: Instant = Clock.System.now()
    )
}

internal abstract class AbstractCongestionController : CongestionController {
    override var congestionWindow: Int = INITIAL_WINDOW_SIZE
        protected set
    override var inFlight: Int = 0
        protected set

    override fun onSent(seqno: Int, now: Instant): PacketMeta {
        inFlight++
        if (inFlight > congestionWindow) {
            println("Warning: in-flight packets exceed congestion window: $inFlight > $congestionWindow")
        }
        return PacketMeta(seqno, now)
    }

    override fun onConfirm(ackSymbols: Collection<PacketMeta>, now: Instant) {
        inFlight -= ackSymbols.size
        if (inFlight < 0) {
            println("Warning: in-flight packets went negative: $inFlight < 0")
            inFlight = 0
        }
    }

    override fun onLost(lostSymbols: Collection<PacketMeta>, now: Instant) {
        inFlight -= lostSymbols.size
    }

    companion object {
        const val INITIAL_WINDOW_SIZE = 10
    }
}

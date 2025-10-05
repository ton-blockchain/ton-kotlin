package org.ton.kotlin.rldp.congestion

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal class RenoCongestionController : AbstractCongestionController() {
    private var slowStartThreshold = Long.MAX_VALUE
    private var bytesAckedSlow = 0L
    private var bytesAckedCA = 0L

    override fun onPacketSent(sentBytes: Long, bytesInFlight: Long, now: Instant) {
    }

    override fun onPacketAck(
        packet: PacketMeta,
        now: Instant,
        rttStats: RttStats
    ) {
        // In Slow slart, bytesAckedSlow is used for counting
        // acknowledged bytes.
        if (congestionWindow < slowStartThreshold) {
            bytesAckedSlow += packet.size
            congestionWindow += MAX_SEND_PAYLOAD_SIZE
        } else {
            // Congestion avoidance.
            bytesAckedCA += packet.size
            if (bytesAckedCA >= congestionWindow) {
                bytesAckedCA -= congestionWindow
                congestionWindow += MAX_SEND_PAYLOAD_SIZE
            }
        }
    }

    override fun congestionEvent(
        bytesInFlight: Long,
        lostBytes: Long,
        largestLostPacket: PacketMeta,
        now: Instant
    ) {
        // Start a new congestion event if packet was sent after the
        // start of the previous congestion recovery period.
        val timeSent = largestLostPacket.sentTime
        if (!inCongestionRecovery(timeSent)) {
            congestionRecoveryStartTime = now
            congestionWindow = (congestionWindow * LOSS_REDUCTION_FACTOR).toLong()
                .coerceAtLeast(MAX_SEND_PAYLOAD_SIZE.toLong() * MINIMUM_WINDOW_PACKETS)
            bytesAckedCA = (congestionWindow * LOSS_REDUCTION_FACTOR).toLong()
            slowStartThreshold = congestionWindow
        }
    }
}

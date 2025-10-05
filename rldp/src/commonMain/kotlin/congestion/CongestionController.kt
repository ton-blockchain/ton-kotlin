@file:OptIn(ExperimentalTime::class)

package org.ton.kotlin.rldp.congestion

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

internal interface CongestionController {
    val congestionWindow: Long

    val pacer: Pacer

    fun onPacketsAck(packets: Iterable<PacketMeta>, now: Instant, rttStats: RttStats) {
        packets.forEach {
            onPacketAck(it, now, rttStats)
        }
    }

    fun onPacketAck(packet: PacketMeta, now: Instant, rttStats: RttStats)

    fun onPacketSent(sentBytes: Long, bytesInFlight: Long, now: Instant)

    fun congestionEvent(bytesInFlight: Long, lostBytes: Long, largestLostPacket: PacketMeta, now: Instant)
}

internal const val MAX_SEND_PAYLOAD_SIZE = 768
internal const val MINIMUM_WINDOW_PACKETS = 2
private const val DEFAULT_INITIAL_CONGESTION_WINDOW_PACKETS = 10
private const val INITIAL_WINDOW_SIZE = MAX_SEND_PAYLOAD_SIZE.toLong() * DEFAULT_INITIAL_CONGESTION_WINDOW_PACKETS
internal const val LOSS_REDUCTION_FACTOR = 0.5

internal abstract class AbstractCongestionController : CongestionController {
    override var congestionWindow: Long = INITIAL_WINDOW_SIZE
        protected set

    protected var congestionRecoveryStartTime: Instant? = null

    override val pacer = Pacer(
        capacity = (DEFAULT_INITIAL_CONGESTION_WINDOW_PACKETS * MAX_SEND_PAYLOAD_SIZE).toLong(),
        rate = 0,
        maxDatagramSize = MAX_SEND_PAYLOAD_SIZE
    )

    fun inCongestionRecovery(sentTime: Instant): Boolean {
        return congestionRecoveryStartTime?.let { sentTime <= it } ?: false
    }
}

internal data class PacketMeta(
    val seqno: Int,
    val sentTime: Instant,
    val size: Int
)

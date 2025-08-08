package org.ton.kotlin.rldp.congestion

/**
 * Tracker for acknowledgments of up to 32 packets per part,
 * using a bitmask to record received sequence numbers.
 */
internal class Ack {
    var maxSeqno: Int = -1
    var receivedMask: Int = 0
    var receivedCount: Int = 0

    /**
     * Record a received packet sequence number.
     *
     * @param seqno Sequence number of the received packet (>=0).
     * @return `true` if this packet was not seen before; `false` otherwise.
     */
    fun onReceivedPacket(seqno: Int): Boolean {
        require(seqno >= 0) { "seqno must be non-negative" }

        if (seqno > maxSeqno) {
            val diff = seqno - maxSeqno
            receivedMask = when {
                diff >= 32 -> 0
                else -> receivedMask shl diff
            }
            receivedMask = receivedMask or 1
            maxSeqno = seqno
            receivedCount++
            return true
        }

        val offset = maxSeqno - seqno
        if (offset in 0 until 32) {
            val bit = 1 shl offset
            if (receivedMask and bit == 0) {
                receivedMask = receivedMask or bit
                receivedCount++
                return true
            }
        }

        return false
    }
}

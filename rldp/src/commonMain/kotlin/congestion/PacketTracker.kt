@file:OptIn(ExperimentalTime::class)

package org.ton.kotlin.rldp.congestion

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

internal class PacketTracker {
    private val packetMap = HashMap<Int, PacketMeta>()
    private val packets = ArrayDeque<PacketMeta>()
    private val lastSeqno get() = packets.lastOrNull()?.seqno ?: -1
    var maxAcked: PacketMeta? = null
        private set

    fun onSent(meta: PacketMeta) {
        if (lastSeqno + 1 == meta.seqno) {
            packets.addLast(meta)
            packetMap[meta.seqno] = meta
        }
    }

    operator fun get(i: Int): PacketMeta? = packetMap[i]

    fun onAck(
        maxSeqno: Int,
        receivedMask: Int,
    ): List<PacketMeta> {
        if (lastSeqno < maxSeqno) {
            return emptyList()
        }

        val ackedPackets = ArrayList<PacketMeta>(32)
        for (i in 0 until 32) {
            if (receivedMask and (1 shl i) != 0) {
                val seqno = maxSeqno - i
                val packet = packetMap.remove(seqno) ?: continue
                ackedPackets.add(packet)
            }
        }
        val newMaxAcked = ackedPackets.lastOrNull()
        if (newMaxAcked != null && (maxAcked?.seqno ?: -1) < newMaxAcked.seqno) {
            maxAcked = newMaxAcked
        }
        return ackedPackets
    }

    fun drop(
        seqno: Int,
        time: Instant
    ): ArrayList<PacketMeta> {
        val droppedPackets = ArrayList<PacketMeta>()
        while (!packets.isEmpty()) {
            val packet = packets.first()
            if (packet.sentTime < time || packet.seqno < seqno) {
                packets.removeFirst()
                packetMap.remove(packet.seqno)
                droppedPackets.add(packet)
            } else {
                break
            }
        }
        return droppedPackets
    }
}

package org.ton.kotlin.rldp

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlinx.io.Source
import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.fec.FecEncoder
import org.ton.kotlin.fec.FecType
import org.ton.kotlin.rldp.congestion.*
import kotlin.math.min
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val PART_SIZE_BYTES = 2_000_000L
private const val SYMBOL_SIZE_BYTES = 768

@OptIn(ExperimentalTime::class)
internal class RldpOutgoingTransfer(
    val transferId: ByteString,
    val totalSize: Long,
    val source: Source,
    val incoming: ReceiveChannel<Rldp2MessagePart>,
    val outgoing: SendChannel<Rldp2MessagePart.Part>,
    val rtt: RttStats,
    val cc: CongestionController = RenoCongestionController()
) {
    private var bytesInFlight = 0L
    var remainingBytes = totalSize
        private set

    suspend fun send() = coroutineScope {
        var partId = 0
        while (remainingBytes > 0 && isActive) {
            val size = min(remainingBytes, PART_SIZE_BYTES).toInt()
            val partSender = PartSender(partId, size)
            partSender.send()
            remainingBytes -= size
            partId++
        }
    }

    private inner class PartSender(
        private val partId: Int,
        partSize: Int,
    ) {
        private val fecType = FecType.RaptorQ(partSize, SYMBOL_SIZE_BYTES)
        private val encoder: FecEncoder = fecType.createEncoder(source)
        private val tracker = PacketTracker()

        private var seqno: Int = 0
        private var isDone: Boolean = false

        @OptIn(ExperimentalCoroutinesApi::class)
        suspend fun send() = coroutineScope {
            while (!isDone && isActive) {
                val now = Clock.System.now()
                onPacingTick(now)
                val delay = cc.pacer.nextTime - now
                select {
                    incoming.onReceive { msg ->
                        onIncoming(msg)
                    }
                    onTimeout(delay) {
                    }
                }
            }
        }

        private suspend fun onPacingTick(now: Instant) {
            if (now < cc.pacer.nextTime) {
                return
            }
            sendSymbol(seqno++)
        }

        private fun onIncoming(msg: Rldp2MessagePart) {
            when (msg) {
                is Rldp2MessagePart.Confirm -> handleConfirm(msg, Clock.System.now())
                is Rldp2MessagePart.Complete -> {
                    isDone = true
                }

                is Rldp2MessagePart.Part -> {
                    /* ignore */
                }
            }
        }

        private suspend fun sendSymbol(seqno: Int) {
            val data = ByteString(*encoder.encodeToByteArray(seqno))
            val msg = Rldp2MessagePart.Part(
                transferId = transferId,
                fecType = encoder.parameters,
                part = partId,
                totalSize = totalSize,
                seqno = seqno,
                data = data
            )
            outgoing.send(msg)
            val sentTime = Clock.System.now()
            cc.onPacketSent(data.size.toLong(), 0, sentTime)
            tracker.onSent(PacketMeta(seqno, sentTime, data.size))
            bytesInFlight += data.size
        }

        private fun handleConfirm(c: Rldp2MessagePart.Confirm, now: Instant) {
            val oldMax = tracker.maxAcked
            val acked = tracker.onAck(c.maxSeqno, c.receivedMask)
            acked.forEach {
                bytesInFlight -= it.size
            }
            val newMax = tracker.maxAcked ?: return

            if (newMax != oldMax) {
                val sample = now - newMax.sentTime
                rtt.onRttSample(sample, now)

                val lossCutoff = (newMax.seqno - 32).coerceAtLeast(0)
                val lossTime = now - (rtt.rtt + rtt.rttVar * 4)
                val lost = tracker.drop(lossCutoff, lossTime)
                val lostBytes = lost.sumOf { it.size.toLong() }
                val largestLostPacket = lost.maxBy { it.seqno }
                cc.congestionEvent(bytesInFlight, lostBytes, largestLostPacket, now)
                cc.onPacketsAck(acked, now, rtt)
                bytesInFlight -= lostBytes
            }
        }
    }
}

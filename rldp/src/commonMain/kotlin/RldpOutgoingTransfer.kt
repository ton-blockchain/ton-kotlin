package org.ton.kotlin.rldp

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import kotlinx.io.Source
import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.fec.FecEncoder
import org.ton.kotlin.fec.FecType
import org.ton.kotlin.rldp.congestion.CongestionController
import org.ton.kotlin.rldp.congestion.NewRenoCongestionController
import org.ton.kotlin.rldp.congestion.PacketTracker
import org.ton.kotlin.rldp.congestion.RttStats
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.math.min
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

const val PART_SIZE_BYTES = 2000000L
const val SYMBOL_SIZE_BYTES = 768

@OptIn(ExperimentalTime::class)
fun CoroutineScope.rldpOutgoingTransfer(
    transferId: ByteString,
    totalSize: Long,
    source: Source,
    incoming: ReceiveChannel<Rldp2MessagePart>,
    outgoing: SendChannel<Rldp2MessagePart.Part>,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.LAZY,
): Job = launch(context, start) {
    val rttStats = RttStats()
    var partIndex = 0
    var remainingBytes = totalSize
    while (remainingBytes > 0) {
        val partDataSize = min(remainingBytes, PART_SIZE_BYTES)
        remainingBytes -= partDataSize
        val fecType = FecType.RaptorQ(partDataSize.toInt(), SYMBOL_SIZE_BYTES)
        val encoder = fecType.createEncoder(source)
        val congestionController = NewRenoCongestionController(rttStats)
        val confirmChannel =
            Channel<Pair<Rldp2MessagePart.Confirm, Instant>>(
                capacity = 32,
                onBufferOverflow = BufferOverflow.DROP_OLDEST
            )

        val completeJob = launch {
            for (msg in incoming) {
                if (msg.part != partIndex) {
                    continue
                }
                when (msg) {
                    is Rldp2MessagePart.Complete -> {
                        confirmChannel.close()
                        break
                    }

                    is Rldp2MessagePart.Confirm -> {
                        confirmChannel.send(msg to Clock.System.now())
                    }

                    is Rldp2MessagePart.Part -> {
                        println("Transfer $transferId received part:${msg.part}, seqno:${msg.seqno}, size:${msg.data.size}, ignoring")
                    }
                }
            }
        }

        val senderJob = launch {
            launch {
                encoder.prepareMoreSymbols()
            }
            encoder
                .rldp2partFlow(transferId, partIndex, totalSize)
                .buffer(1)
                .withCongestionControl(confirmChannel, congestionController, rttStats)
                .collect {
                    if (!completeJob.isCompleted) {
                        outgoing.send(it)
                    }
                }
        }

        completeJob.join()
        senderJob.cancelAndJoin()

        partIndex++
    }

    outgoing.close()
}

@OptIn(ExperimentalTime::class)
private fun Flow<Rldp2MessagePart.Part>.withCongestionControl(
    confirmChannel: ReceiveChannel<Pair<Rldp2MessagePart.Confirm, Instant>>,
    congestionController: CongestionController,
    rtt: RttStats,
    tracker: PacketTracker = PacketTracker()
): Flow<Rldp2MessagePart.Part> {
    fun processConfirm(confirm: Rldp2MessagePart.Confirm, now: Instant) {
        val oldMax = tracker.maxAcked
        val acked = tracker.onAck(confirm.maxSeqno, confirm.receivedMask)
        val newMax = tracker.maxAcked

        if (newMax != null && newMax != oldMax) {
            val rttSample = now - newMax.sentTime
            rtt.onRttSample(rttSample, now)

            val lossCutoff = (newMax.seqno - 32).coerceAtLeast(0)
            val lossTime = now - (rtt.smoothedRtt + rtt.rttVar * 4)
            val lost = tracker.drop(lossCutoff, lossTime)

            congestionController.onLost(lost, now)
            congestionController.onConfirm(acked, now)
        }
    }

    return transform { symbol ->
        while (true) {
            val (confirm, now) = confirmChannel.tryReceive().getOrNull() ?: break
            processConfirm(confirm, now)
        }

        while (!congestionController.canSend()) {
            val pacingDelay = congestionController.pacingDelay()
            val (confirm, now) = if (!pacingDelay.isPositive()) {
                confirmChannel.receive()
            } else {
                withTimeoutOrNull(pacingDelay) { confirmChannel.receive() } ?: break
            }
            processConfirm(confirm, now)
        }

        emit(symbol)
        val packetMeta = congestionController.onSent(symbol.seqno)
        tracker.onSent(packetMeta)
    }
}

private fun FecEncoder.rldp2partFlow(
    transferId: ByteString,
    partIndex: Int,
    totalSize: Long,
) = flow {
    val fecType = parameters
    var seqno = 0
    while (true) {
        val symbolData = ByteString(*encodeToByteArray(seqno))
        emit(Rldp2MessagePart.Part(transferId, fecType, partIndex, totalSize, seqno, symbolData))
        seqno++
    }
}

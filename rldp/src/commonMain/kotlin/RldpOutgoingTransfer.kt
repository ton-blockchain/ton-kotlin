package org.ton.kotlin.rldp

import io.ktor.util.logging.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.Source
import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.fec.FecEncoder
import org.ton.kotlin.fec.FecType
import org.ton.kotlin.rldp.congestion.CongestionController
import org.ton.kotlin.rldp.congestion.NewRenoCongestionController
import org.ton.kotlin.rldp.congestion.PacketTracker
import org.ton.kotlin.rldp.congestion.RttStats
import kotlin.math.min
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

const val PART_SIZE_BYTES = 2000000L
const val SYMBOL_SIZE_BYTES = 768

private val LOGGER = KtorSimpleLogger("rldpOutgoingTransfer")

@OptIn(ExperimentalTime::class)
internal suspend fun rldpOutgoingTransfer(
    transferId: ByteString,
    totalSize: Long,
    source: Source,
    incoming: ReceiveChannel<Rldp2MessagePart>,
    outgoing: SendChannel<Rldp2MessagePart.Part>,
) = coroutineScope {
    val rttStats = RttStats()
    var partIndex = 0
    var remainingBytes = totalSize
    while (remainingBytes > 0) {
        LOGGER.trace { "${transferId.debugString()} start part $partIndex" }
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
                LOGGER.trace { "${transferId.debugString()} incoming message: $msg" }
                if (msg.part != partIndex) {
                    continue
                }
                when (msg) {
                    is Rldp2MessagePart.Complete -> {
                        break
                    }

                    is Rldp2MessagePart.Confirm -> {
                        LOGGER.trace { "${transferId.debugString()} sending to confirmChannel" }
                        confirmChannel.send(msg to Clock.System.now())
                        LOGGER.trace { "${transferId.debugString()} sent to confirmChannel" }
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
                .onEach {
                    LOGGER.trace { "${transferId.debugString()} generated part: ${it.seqno} ${it.data}" }
                }
                .withCongestionControl(confirmChannel, congestionController, rttStats)
                .collect {
                    if (!completeJob.isCompleted) {
                        LOGGER.trace { "${transferId.debugString()} outgoing message: $it" }
                        outgoing.send(it)
                        LOGGER.trace { "${transferId.debugString()} outgoing message sent: $it" }
                    }
                }
        }

        completeJob.join()
        LOGGER.trace { "${transferId.debugString()} completeJob joined" }
        senderJob.cancelAndJoin()
        LOGGER.trace { "${transferId.debugString()} senderJob cancelled" }
        confirmChannel.close()
        LOGGER.trace { "${transferId.debugString()} confirmChannel closed" }

        partIndex++
    }
    LOGGER.trace { "${transferId.debugString()} done outgoing transfer" }
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

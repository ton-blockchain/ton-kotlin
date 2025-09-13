package org.ton.kotlin.rldp

import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.withContext
import kotlinx.io.Sink
import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.rldp.congestion.Ack
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val LOGGER = KtorSimpleLogger("rldpIncomingTransfer")

@OptIn(ExperimentalTime::class)
internal suspend fun rldpIncomingTransfer(
    transferId: ByteString,
    sink: Sink,
    incoming: ReceiveChannel<Rldp2MessagePart>,
    outgoing: SendChannel<Rldp2MessagePart.Acknowledgment>,
) = withContext(CoroutineName("rldpIncomingTransfer-${transferId.debugString()}")) {
    var partIndex = 0
    var lastSentComplete = Instant.DISTANT_PAST
    var remainingBytes = Long.MAX_VALUE
    while (remainingBytes > 0) {
        val first = incoming.receive() as? Rldp2MessagePart.Part ?: run {
            println("Received non-part message, expected part at index $partIndex")
            continue
        }
        if (first.seqno < partIndex) {
            val now = Clock.System.now()
            if (now - lastSentComplete > 10.milliseconds) {
                outgoing.send(Rldp2MessagePart.Complete(transferId, partIndex))
                lastSentComplete = now
            }
        }
        if (first.part != partIndex) {
            continue
        }
        if (remainingBytes == Long.MAX_VALUE) {
            remainingBytes = first.totalSize
        }
        if (first.totalSize != remainingBytes) {
            continue
        }
        val fecType = first.fecType

        val decoderJob = async {
            val decoder = fecType.createDecoder()
            val result = ByteArray(fecType.dataSize)
            val ack = Ack()

            fun processSymbol(symbol: Rldp2MessagePart.Part): Boolean {
                LOGGER.trace { "start processing ${symbol.seqno}" }
                if (symbol.part != partIndex) return false
                if (symbol.fecType != fecType) return false
                if (!ack.onReceivedPacket(symbol.seqno)) return false
                val canTryDecode = decoder.addSymbol(symbol.seqno, symbol.data.toByteArray())
                if (!canTryDecode) return false
                return decoder.decodeFullyIntoByteArray(result)
            }

            loop@ while (true) {
                var symbol = incoming.receive()
                if (symbol !is Rldp2MessagePart.Part) continue
                if (processSymbol(symbol)) {
                    break
                }
                var i = 1
                while (true) {
                    symbol = incoming.tryReceive().getOrNull() ?: break
                    if (symbol !is Rldp2MessagePart.Part) continue
                    i++
                    if (processSymbol(symbol)) {
                        break@loop
                    }
                }
                if (i > 1) {
                    LOGGER.trace { "send confirmation with $i" }
                }
                val confirm = Rldp2MessagePart.Confirm(
                    transferId,
                    partIndex,
                    ack.maxSeqno,
                    ack.receivedMask,
                    ack.receivedCount
                )
                outgoing.send(confirm)
            }

            result
        }

        val result = decoderJob.await()
        remainingBytes -= result.size

        LOGGER.trace { "${transferId.debugString()} received result, try to send complete" }
        outgoing.send(Rldp2MessagePart.Complete(transferId, partIndex))
        LOGGER.trace { "${transferId.debugString()} sent complete, write to sink..." }
        lastSentComplete = Clock.System.now()

        sink.write(result)

        partIndex++
    }

    LOGGER.trace { "${transferId.debugString()} done rldp transfer" }
}

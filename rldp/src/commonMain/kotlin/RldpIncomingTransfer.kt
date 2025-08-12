package org.ton.kotlin.rldp

import io.ktor.util.logging.*
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
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
) = coroutineScope {
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

        val symbolDecoderChannel = Channel<Rldp2MessagePart.Part>(Channel.CONFLATED)
        val decoderJob = async {
            val decoder = fecType.createDecoder()
            val result = ByteArray(fecType.dataSize)
            for (symbol in symbolDecoderChannel) {
                LOGGER.trace { "${transferId.debugString()} received in decoder: ${symbol.seqno}" }
                val canTryDecode = decoder.addSymbol(symbol.seqno, symbol.data.toByteArray())
                if (!canTryDecode) continue
                if (decoder.decodeFullyIntoByteArray(result)) {
                    break
                }
            }
            symbolDecoderChannel.close()
            LOGGER.trace { "${transferId.debugString()} closing decoder..." }
            result
        }

        val ack = Ack()
        var lastSentConfirm = Instant.DISTANT_PAST
        var ackSinceLastConfirm = 0
        fun trySendConfirm(now: Instant) {
            if (ackSinceLastConfirm++ < 8 || now - lastSentConfirm < 10.milliseconds) {
                return
            }
            outgoing.trySend(
                Rldp2MessagePart.Confirm(
                    transferId,
                    partIndex,
                    ack.maxSeqno,
                    ack.receivedMask,
                    ack.receivedCount
                )
            ).onSuccess {
                lastSentConfirm = now
                ackSinceLastConfirm = 0
            }
        }

        ack.onReceivedPacket(first.seqno)
        trySendConfirm(Clock.System.now())

        var result = byteArrayOf()
        LOGGER.trace { "${transferId.debugString()} start select loop" }
        while (result.isEmpty()) {
            LOGGER.trace { "${transferId.debugString()} loop iter" }
            select {
                decoderJob.onAwait {
                    LOGGER.trace { "${transferId.debugString()} got result" }
                    result = it
                }
                incoming.onReceive { symbol ->
                    LOGGER.trace { "${transferId.debugString()} got symbol" }
                    if (symbol !is Rldp2MessagePart.Part) {
                        println("Received unexpected part: $symbol, expected part index $partIndex")
                        return@onReceive
                    }
                    if (symbol.part != partIndex) return@onReceive
                    if (symbol.fecType != fecType) return@onReceive
                    if (!ack.onReceivedPacket(symbol.seqno)) return@onReceive
                    trySendConfirm(Clock.System.now())
                    LOGGER.trace { "${symbol.transferId.debugString()} received symbol: ${symbol.seqno}, sending to decoder" }
                    symbolDecoderChannel.trySend(symbol).onClosed {
                        LOGGER.trace { "${symbol.transferId.debugString()} onClosed" }
                        result = decoderJob.await()
                    }.onFailure {
                        LOGGER.trace { "${symbol.transferId.debugString()} onFailure, ${it?.stackTraceToString()}" }
                    }.onSuccess {
                        LOGGER.trace { "${symbol.transferId.debugString()} onSuccess symbol: ${symbol.seqno}" }
                    }
                }
            }
        }
        remainingBytes -= result.size

        LOGGER.trace { "${transferId.debugString()} received result, try to send complete" }
        outgoing.send(Rldp2MessagePart.Complete(transferId, partIndex))
        LOGGER.trace { "${transferId.debugString()} sent complete, write to sink..." }
        lastSentComplete = Clock.System.now()

        sink.write(result)

        partIndex++
    }

    LOGGER.trace { "done rldp transfer" }
}

package org.ton.kotlin.rldp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.io.Sink
import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.rldp.congestion.Ack
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun CoroutineScope.rldpIncomingTransfer(
    transferId: ByteString,
    sink: Sink,
    incoming: ReceiveChannel<Rldp2MessagePart>,
    outgoing: SendChannel<Rldp2MessagePart.Acknowledgment>,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.LAZY,
) = launch(context, start) {
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
                val canTryDecode = decoder.addSymbol(symbol.seqno, symbol.data.toByteArray())
                if (!canTryDecode) continue
                if (decoder.decodeFullyIntoByteArray(result)) {
                    break
                }
            }
            symbolDecoderChannel.close()
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
        while (result.isEmpty()) {
            select {
                decoderJob.onAwait {
                    remainingBytes -= it.size.toLong()
                    result = it
                }
                incoming.onReceive { symbol ->
                    if (symbol !is Rldp2MessagePart.Part) {
                        println("Received unexpected part: $symbol, expected part index $partIndex")
                        return@onReceive
                    }
                    if (symbol.part != partIndex) return@onReceive
                    if (symbol.fecType != fecType) return@onReceive
                    if (!ack.onReceivedPacket(symbol.seqno)) return@onReceive
                    trySendConfirm(Clock.System.now())
                    symbolDecoderChannel.send(symbol)
                }
            }
        }

        outgoing.send(Rldp2MessagePart.Complete(transferId, partIndex))
        lastSentComplete = Clock.System.now()

        sink.write(result)

        partIndex++
    }

    outgoing.close()
}

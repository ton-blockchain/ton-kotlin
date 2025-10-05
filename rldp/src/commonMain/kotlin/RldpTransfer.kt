package org.ton.kotlin.rldp

import io.ktor.util.logging.*
import kotlinx.coroutines.channels.*
import kotlinx.io.*
import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.rldp.congestion.RttStats
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
internal class RldpTransfer(
    val id: ByteString,
    val rldp: RldpConnection,
    val outgoing: SendChannel<Rldp2MessagePart>,
) {
    private val logger = KtorSimpleLogger("org.ton.kotlin.rldp.RldpTransfer")
    private val incoming: Channel<Rldp2MessagePart> = Channel(Channel.BUFFERED)
    var lastActive = Instant.DISTANT_PAST
        private set

    suspend fun receive(): ByteString {
        val buffer = Buffer()
        receive(buffer)
        return buffer.readByteString()
    }

    suspend fun receive(sink: Sink) {
        logger.trace { "${id.debugString()} start receiving" }

        val transfer = RldpIncomingTransfer(id, sink, incoming, outgoing)
        val duration = measureTime {
            try {
                transfer.receive()
            } finally {
                incoming.close()
            }
        }
        val transferredBytes = transfer.totalSize - transfer.remainingBytes
        val bandwidth = Bandwidth.fromBytesPerPeriod(transferredBytes, duration)
        logger.trace { "${id.debugString()} end receiving, duration: $duration, bandwidth: $bandwidth" }
    }

    suspend fun send(byteString: ByteString) {
        val buffer = Buffer()
        buffer.write(byteString)
        try {
            send(buffer, byteString.size.toLong())
        } finally {
            incoming.close()
        }
    }

    suspend fun send(source: Source, totalSize: Long) {
        logger.trace { "${id.debugString()} start sending, totalSize: $totalSize" }
        val rtt = RttStats()
        val transfer = RldpOutgoingTransfer(id, totalSize, source, incoming, outgoing, rtt)
        val duration = measureTime {
            try {
                transfer.send()
            } finally {
                incoming.close()
            }
        }
        val transferredBytes = transfer.totalSize - transfer.remainingBytes
        val bandwidth = Bandwidth.fromBytesPerPeriod(transferredBytes, duration)
        logger.trace { "${id.debugString()} end sending, duration: $duration, bandwidth: $bandwidth" }
    }

    suspend fun handleMessagePart(
        messagePart: Rldp2MessagePart
    ) {
        lastActive = Clock.System.now()
        logger.trace { "${id.debugString()} try to handle incoming: $messagePart" }
        incoming.trySend(messagePart).onClosed {
            if (messagePart is Rldp2MessagePart.Part) {
                logger.trace { "${id.debugString()} send complete, because incoming closed" }
                rldp.sendMessagePart(Rldp2MessagePart.Complete(id, messagePart.part))
            }
        }.onFailure {
            if (messagePart is Rldp2MessagePart.Part && it is ClosedSendChannelException) {
                logger.trace { "${id.debugString()} send complete, because incoming ClosedSendChannelException" }
                rldp.sendMessagePart(Rldp2MessagePart.Complete(id, messagePart.part))
            }
        }
    }
}

package org.ton.kotlin.rldp

import io.ktor.util.logging.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.*
import kotlinx.io.*
import kotlinx.io.bytestring.ByteString
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal class RldpTransfer(
    val id: ByteString,
    val rldp: RldpConnection,
    val outgoing: SendChannel<Rldp2MessagePart>,
) {
    private val logger = KtorSimpleLogger("org.ton.kotlin.rldp.RldpTransfer")
    private var transferJob: Job? = null
    private val incoming: Channel<Rldp2MessagePart> = Channel(Channel.BUFFERED)
    var lastActive = Instant.DISTANT_PAST
        private set

    suspend fun receive(): ByteString {
        val buffer = Buffer()
        receive(buffer)
        return buffer.readByteString()
    }

    suspend fun receive(sink: Sink) {
        check(transferJob == null) { "Transfer $id is already in progress" }
        logger.trace { "${id.debugString()} start receiving" }
        rldpIncomingTransfer(id, sink, incoming, outgoing)
        incoming.close()
        logger.trace { "${id.debugString()} end receiving" }
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
        check(transferJob == null) { "Transfer $id is already in progress" }
        logger.trace { "${id.debugString()} start sending, totalSize: $totalSize" }
        try {
            rldpOutgoingTransfer(id, totalSize, source, incoming, outgoing)
        } finally {
            incoming.close()
        }
        logger.trace { "${id.debugString()} end sending, totalSize: $totalSize" }
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

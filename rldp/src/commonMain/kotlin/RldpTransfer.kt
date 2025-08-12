package org.ton.kotlin.rldp

import io.ktor.util.logging.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.*
import kotlinx.io.*
import kotlinx.io.bytestring.ByteString

internal class RldpTransfer(
    val id: ByteString,
    val rldp: RldpConnection,
    val outgoing: SendChannel<Rldp2MessagePart>,
) {
    private val logger = KtorSimpleLogger("RldpTransfer")
    private var transferJob: Job? = null
    private val incoming: Channel<Rldp2MessagePart> =
        Channel(Channel.BUFFERED, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private var role = ""

    suspend fun receive(): ByteString {
        val buffer = Buffer()
        receive(buffer)
        return buffer.readByteString()
    }

    suspend fun receive(sink: Sink) {
        check(transferJob == null) { "Transfer $id is already in progress" }
        role = "receive"
        logger.trace { "${id.debugString()} start receiving" }
        rldpIncomingTransfer(id, sink, incoming, outgoing)
        incoming.close()
        logger.trace { "${id.debugString()} $role end receiving" }
    }

    suspend fun send(byteString: ByteString) {
        val buffer = Buffer()
        buffer.write(byteString)
        send(buffer, byteString.size.toLong())
    }

    suspend fun send(source: Source, totalSize: Long) {
        check(transferJob == null) { "Transfer $id is already in progress" }
        role = "send   "
        logger.trace { "${id.debugString()} start sending, totalSize: $totalSize" }
        rldpOutgoingTransfer(id, totalSize, source, incoming, outgoing)
        incoming.close()
        logger.trace { "${id.debugString()} end sending, totalSize: $totalSize" }
    }

    fun handleMessagePart(
        messagePart: Rldp2MessagePart
    ) {
        logger.trace { "${id.debugString()} try to handle incoming: $messagePart" }
        incoming.trySend(messagePart).onClosed {
            if (messagePart is Rldp2MessagePart.Part) {
                outgoing.trySend(Rldp2MessagePart.Complete(id, messagePart.part))
            }
        }.onFailure {
            logger.error("${id.debugString()} failed to send: $it")
        }
    }
}

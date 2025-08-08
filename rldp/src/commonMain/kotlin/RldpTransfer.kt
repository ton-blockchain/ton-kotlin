package org.ton.kotlin.rldp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.io.*
import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.tl.TL
import kotlin.coroutines.CoroutineContext

internal class RldpTransfer(
    val id: ByteString,
    val rldp: RldpConnection,
) : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext = rldp.coroutineContext + job
    private var transferJob: Job? = null
    private val incoming: Channel<Rldp2MessagePart> = Channel()
    private val outgoing: Channel<Rldp2MessagePart> = Channel()

    init {
        launch {
            for (message in outgoing) {
                val rawMessage = TL.Boxed.encodeToByteString(Rldp2MessagePart.serializer(), message)
                rldp.adnl.message(rawMessage)
            }
        }
    }

    suspend fun receive(): ByteString {
        val buffer = Buffer()
        receive(buffer)
        return buffer.readByteString()
    }

    suspend fun receive(sink: Sink) {
        check(transferJob == null) { "Transfer $id is already in progress" }
        rldpIncomingTransfer(id, sink, incoming, outgoing, coroutineContext).also {
            transferJob = it
        }.join()
        job.complete()
    }

    suspend fun send(byteString: ByteString) {
        val buffer = Buffer()
        buffer.write(byteString)
        send(buffer, byteString.size.toLong())
    }

    suspend fun send(source: Source, totalSize: Long) {
        check(transferJob == null) { "Transfer $id is already in progress" }
        rldpOutgoingTransfer(id, totalSize, source, incoming, outgoing, coroutineContext).also {
            transferJob = it
        }.join()
        job.complete()
    }

    suspend fun handleMessagePart(
        messagePart: Rldp2MessagePart
    ) {
        incoming.send(messagePart)
    }
}

package org.ton.proxy.rldp

import io.ktor.utils.io.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import org.ton.kotlin.adnl.fec.FecRaptorQ
import org.ton.kotlin.adnl.rldp.RldpComplete
import org.ton.kotlin.adnl.rldp.RldpConfirm
import org.ton.kotlin.adnl.rldp.RldpMessagePart
import org.ton.kotlin.adnl.rldp.RldpMessagePartData
import org.ton.kotlin.bitstring.BitString
import org.ton.proxy.rldp.fec.raptorq.RaptorQFecDecoder
import kotlin.jvm.JvmStatic

interface RldpInputTransfer : RldpReceiver {
    val id: BitString
    val byteChannel: ByteReadChannel

    override fun receiveRldpMessagePart(message: RldpMessagePart)

    fun transferPackets(): Flow<RldpMessagePart>

    companion object {
        @JvmStatic
        fun of(
            id: BitString
        ): RldpInputTransfer = RldpInputTransferImpl(id)
    }
}

private class RldpInputTransferImpl(
    override val id: BitString,
    override val byteChannel: ByteChannel = ByteChannel(true),
) : RldpInputTransfer {
    private val messagesChannel = Channel<RldpMessagePartData>(Channel.UNLIMITED)

    override fun transferPackets(): Flow<RldpMessagePart> = flow {
        var decoder: RaptorQFecDecoder? = null
        var totalSize = -1L
        var part = 0
        var processed = -2L
        var confirmCount = 0
        try {
            while (processed < totalSize && currentCoroutineContext().isActive) {
                val message = messagesChannel.receive()
                if (message.part != part) {
                    continue
                }

                if (totalSize == -1L) {
                    totalSize = message.total_size
                    processed = 0
                } else {
                    require(totalSize == message.total_size) { "Total size mismatch, expected: $totalSize, actual: ${message.total_size}" }
                }

                var currentDecoder = decoder
                if (currentDecoder == null) {
                    currentDecoder = RaptorQFecDecoder(message.fec_type as FecRaptorQ)
                    decoder = currentDecoder
                } else {
                    require(currentDecoder.fecType == message.fec_type) { "Fec type mismatch, expected: ${currentDecoder.fecType}, actual: ${message.fec_type}" }
                }

                if (currentDecoder.addSymbol(message.seqno, message.data)) {
                    val payload = ByteArray(message.fec_type.data_size)
                    currentDecoder.decode(payload)
                    val complete = RldpComplete(id, message.part)
                    emit(complete)
                    part++
                    processed += payload.size
                    decoder = null
                    byteChannel.writeFully(payload)
                } else {
                    if (++confirmCount >= 5) {
                        val confirm = RldpConfirm(id, message.part, message.seqno)
                        emit(confirm)
                        confirmCount = 0
                    }
                }
            }
        } finally {
            byteChannel.close()
            messagesChannel.close()
        }
    }

    override fun receiveRldpMessagePart(message: RldpMessagePart) {
        when (message) {
            is RldpMessagePartData -> receiveRldpMessagePartData(message)
            else -> throw IllegalArgumentException("Unsupported message type: ${message::class}")
        }
    }

    fun receiveRldpMessagePartData(message: RldpMessagePartData) {
        messagesChannel.trySend(message)
    }
}

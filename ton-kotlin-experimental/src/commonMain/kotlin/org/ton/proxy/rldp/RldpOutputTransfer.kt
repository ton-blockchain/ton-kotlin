package org.ton.proxy.rldp

import io.ktor.utils.io.core.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import org.ton.kotlin.adnl.rldp.RldpComplete
import org.ton.kotlin.adnl.rldp.RldpMessagePart
import org.ton.kotlin.adnl.rldp.RldpMessagePartData
import org.ton.kotlin.bitstring.BitString
import org.ton.proxy.rldp.fec.raptorq.RaptorQFecEncoder
import kotlin.jvm.JvmStatic
import kotlin.random.Random

interface RldpOutputTransfer : RldpReceiver {
    val id: BitString
    val totalSize: Long

    fun transferPackets(): Flow<RldpMessagePart>

    fun progress(): Flow<Double>

    override fun receiveRldpMessagePart(message: RldpMessagePart)

    companion object {
        @JvmStatic
        fun of(
            data: ByteArray,
            id: BitString = BitString(Random.nextBytes(32))
        ): RldpOutputTransfer = RldOutputTransferImpl(data.size.toLong(), ByteReadPacket(data), id)

        @JvmStatic
        fun of(
            totalSize: Long,
            input: Input,
            id: BitString = BitString(Random.nextBytes(32)),
        ): RldpOutputTransfer = RldOutputTransferImpl(totalSize, input, id)
    }
}

private class RldOutputTransferImpl(
    override val totalSize: Long,
    private val input: Input,
    override val id: BitString,
) : RldpOutputTransfer {
    var part = 0

    private val ackParts = Channel<RldpMessagePart>(Channel.UNLIMITED)

    override fun transferPackets(): Flow<RldpMessagePart> = flow {
        while (currentCoroutineContext().isActive) {
            val bytes = input.readBytesOf(max = SLICE_SIZE)
            if (bytes.isEmpty()) {
                break
            }
            val encoder = RaptorQFecEncoder(bytes)
            part@ for (i in 0 until encoder.fecType.symbol_count + 50) {
                while (currentCoroutineContext().isActive) {
                    val ackMessage = ackParts.tryReceive().getOrNull() ?: break
                    if (ackMessage is RldpComplete && ackMessage.part == part) {
                        break@part
                    }
                }
                val symbol = encoder.encode(i)
                val packet = RldpMessagePartData(
                    transfer_id = id,
                    part = part,
                    seqno = i,
                    fec_type = encoder.fecType,
                    data = symbol,
                    total_size = totalSize,
                )
                emit(packet)
            }
            part++
        }
    }

    override fun receiveRldpMessagePart(message: RldpMessagePart) {
        ackParts.trySend(message)
    }

    override fun progress(): Flow<Double> {
        TODO("Not yet implemented")
    }


    override fun toString(): String = "RldpOutputTransfer(id=$id, totalSize=$totalSize)"

    companion object {
        const val SLICE_SIZE = 2_000_000
        const val SYMBOL_SIZE = 768
        const val WINDOW_SIZE = 1000
        const val WAVES_INTERVAL_MILLISECONDS = 10L
    }
}

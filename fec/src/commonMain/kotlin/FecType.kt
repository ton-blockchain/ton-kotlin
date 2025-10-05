package org.ton.kotlin.fec

import io.github.andreypfau.raptorq.Parameters
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.TlConstructorId

@Serializable
sealed interface FecType {
    val dataSize: Int
    val symbolSize: Int
    val symbolsCount: Int

    fun createEncoder(source: Source): FecEncoder

    fun createDecoder(): FecDecoder

    @Serializable
    @SerialName("fec.raptorQ")
    @TlConstructorId(0x8b93a7e0)
    data class RaptorQ(
        override val dataSize: Int,
        override val symbolSize: Int,
        override val symbolsCount: Int = (dataSize + symbolSize - 1) / symbolSize
    ) : FecType {
        override fun createEncoder(source: Source) =
            RaptorQEncoder(symbolSize, source.readByteArray(dataSize))

        override fun createDecoder() =
            RaptorQDecoder(this)
    }

    @Serializable
    @SerialName("fec.roundRobin")
    @TlConstructorId(0x32f528e4)
    data class RoundRobin(
        override val dataSize: Int,
        override val symbolSize: Int,
        override val symbolsCount: Int
    ) : FecType {
        override fun createEncoder(source: Source): FecEncoder {
            TODO("Not yet implemented")
        }

        override fun createDecoder(): FecDecoder {
            TODO("Not yet implemented")
        }
    }

    @Serializable
    @SerialName("fec.online")
    @TlConstructorId(0x0127660c)
    data class Online(
        override val dataSize: Int,
        override val symbolSize: Int,
        override val symbolsCount: Int
    ) : FecType {
        override fun createEncoder(source: Source): FecEncoder {
            TODO("Not yet implemented")
        }

        override fun createDecoder(): FecDecoder {
            TODO("Not yet implemented")
        }
    }
}

sealed class FecEncoder {
    abstract val parameters: FecType

    abstract fun encodeIntoByteArray(
        seqno: Int,
        destination: ByteArray,
        destinationOffset: Int = 0,
    )

    abstract fun encodeToByteArray(seqno: Int): ByteArray

    abstract suspend fun prepareMoreSymbols()
}

sealed class FecDecoder {
    abstract val parameters: FecType

    abstract fun addSymbol(
        seqno: Int,
        source: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ): Boolean

    abstract fun decodeFullyIntoByteArray(
        destination: ByteArray,
        destinationOffset: Int = 0,
    ): Boolean

    abstract fun solvedEncoderOrNull(): FecEncoder?
}


class RaptorQEncoder(
    private val encoder: io.github.andreypfau.raptorq.Encoder
) : FecEncoder() {
    constructor(
        symbolSize: Int,
        data: ByteArray
    ) : this(io.github.andreypfau.raptorq.Encoder(symbolSize, data))

    constructor(
        symbolSize: Int,
        data: Buffer,
    ) : this(io.github.andreypfau.raptorq.Encoder(symbolSize, data.readByteArray()))

    override val parameters
        get() = FecType.RaptorQ(
            encoder.dataSize,
            encoder.symbolSize,
        )

    override fun encodeToByteArray(seqno: Int): ByteArray {
        return encoder.encodeToByteArray(seqno)
    }

    override fun encodeIntoByteArray(
        seqno: Int,
        destination: ByteArray,
        destinationOffset: Int,
    ) {
        encoder.encodeIntoByteArray(seqno, destination, destinationOffset)
    }

    override suspend fun prepareMoreSymbols() {
        encoder.solve()
    }
}

class RaptorQDecoder(
    override val parameters: FecType.RaptorQ,
    val decoder: io.github.andreypfau.raptorq.Decoder
) : FecDecoder() {
    constructor(parameters: FecType.RaptorQ) : this(
        parameters,
        io.github.andreypfau.raptorq.Decoder(
            Parameters.fromK(parameters.symbolsCount),
            parameters.dataSize,
            parameters.symbolSize
        )
    )

    override fun addSymbol(
        seqno: Int,
        source: ByteArray,
        startIndex: Int,
        endIndex: Int
    ): Boolean {
        return decoder.addSymbol(seqno, source, startIndex, endIndex)
    }

    override fun decodeFullyIntoByteArray(
        destination: ByteArray,
        destinationOffset: Int,
    ): Boolean {
        return decoder.decodeFullyIntoByteArray(destination, destinationOffset)
    }

    override fun solvedEncoderOrNull(): FecEncoder? {
        return decoder.solvedEncoderOrNull()?.let { RaptorQEncoder(it) }
    }
}

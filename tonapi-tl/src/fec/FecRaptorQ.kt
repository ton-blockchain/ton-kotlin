package org.ton.api.fec

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.TlCodec
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter

@SerialName("fec.raptorQ")
@Serializable
public data class FecRaptorQ(
    @SerialName("data_size")
    override val dataSize: Int,
    @SerialName("symbol_size")
    override val symbolSize: Int,
    @SerialName("symbol_count")
    override val symbolCount: Int
) : FecType {
    init {
        FecType.check(this)
    }

    override fun tlCodec(): TlCodec<FecRaptorQ> = Companion

    public companion object : TlConstructor<FecRaptorQ>(
        schema = "fec.raptorQ data_size:int symbol_size:int symbols_count:int = fec.Type",
    ) {
        override fun encode(output: TlWriter, value: FecRaptorQ) {
            output.writeInt(value.dataSize)
            output.writeInt(value.symbolSize)
            output.writeInt(value.symbolCount)
        }

        override fun decode(input: TlReader): FecRaptorQ {
            val dataSize = input.readInt()
            val symbolSize = input.readInt()
            val symbolCount = input.readInt()
            return FecRaptorQ(dataSize, symbolSize, symbolCount)
        }
    }
}

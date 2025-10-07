package org.ton.api.fec

import kotlinx.serialization.Serializable
import org.ton.tl.TlCombinator
import org.ton.tl.TlObject

@Serializable
public sealed interface FecType : TlObject<FecType> {
    public val dataSize: Int
    public val symbolSize: Int
    public val symbolCount: Int

    public companion object : TlCombinator<FecType>(
        FecType::class,
        FecRaptorQ::class to FecRaptorQ,
    ) {
        public fun check(fecType: FecType) {
            require(fecType.symbolSize != 0) { "expected symbol_size != 0, actual: ${fecType.symbolSize}" }
            require(fecType.symbolSize <= 1 shl 11) { "symbol_size must be less than ${1 shl 11}, actual: ${fecType.symbolSize}" }
            val expectedSymbolCount = (fecType.dataSize + fecType.symbolSize - 1) / fecType.symbolSize
            if (fecType.symbolCount != expectedSymbolCount) {
                throw IllegalArgumentException("expected symbol_count: $expectedSymbolCount, actual: ${fecType.symbolCount}")
            }
        }
    }
}

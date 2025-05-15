package org.ton.kotlin.contract

import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.loadRef
import org.ton.kotlin.cell.storeRef
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider
import org.ton.kotlin.tlb.providers.TlbConstructorProvider

public sealed interface SnakeData {
    public companion object : TlbCombinatorProvider<SnakeData> by SnakeDataTlbCombinator

    private object SnakeDataTlbCombinator : TlbCombinator<SnakeData>(
        SnakeData::class,
        SnakeDataTail::class to SnakeDataTail,
        SnakeDataCons::class to SnakeDataCons
    ) {
        override fun findTlbLoaderOrNull(cellSlice: CellSlice): TlbLoader<out SnakeData> {
            return if (cellSlice.refs.lastIndex >= cellSlice.refsPosition) {
                SnakeDataCons // More references available, this is a cons
            } else {
                SnakeDataTail // No more refs, this has to be a tail
            }
        }
    }
}

public data class SnakeDataTail(
    val bits: BitString
) : SnakeData {
    public companion object : TlbConstructorProvider<SnakeDataTail> by SnakeDataTailTlbConstructor

    private object SnakeDataTailTlbConstructor : TlbConstructor<SnakeDataTail>(
        schema = "tail#_ {bn:#} b:(bits bn) = SnakeData 0;"
    ) {
        override fun storeTlb(cellBuilder: CellBuilder, value: SnakeDataTail) {
            cellBuilder.storeBits(value.bits)
        }

        override fun loadTlb(cellSlice: CellSlice): SnakeDataTail =
            SnakeDataTail(cellSlice.loadBits(cellSlice.bits.size - cellSlice.bitsPosition))
    }
}

public data class SnakeDataCons(
    val bits: BitString,
    val next: SnakeData
) : SnakeData {
    public companion object : TlbConstructorProvider<SnakeDataCons> by SnakeDataConsTlbConstructor

    private object SnakeDataConsTlbConstructor : TlbConstructor<SnakeDataCons>(
        schema = "cons#_ {bn:#} {n:#} b:(bits bn) next:^(SnakeData ~n) = SnakeData (n + 1);"
    ) {
        override fun storeTlb(cellBuilder: CellBuilder, value: SnakeDataCons) {
            cellBuilder.storeBits(value.bits)
            cellBuilder.storeRef {
                storeTlb(SnakeData, value.next)
            }
        }

        override fun loadTlb(cellSlice: CellSlice) =
            SnakeDataCons(
                cellSlice.loadBits(cellSlice.bits.size - cellSlice.bitsPosition),
                cellSlice.loadRef {
                    loadTlb(SnakeData)
                }
            )
    }
}

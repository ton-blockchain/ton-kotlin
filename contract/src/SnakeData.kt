package org.ton.contract

import org.ton.bitstring.BitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.loadRef
import org.ton.cell.storeRef
import org.ton.tlb.*
import org.ton.tlb.providers.TlbCombinatorProvider
import org.ton.tlb.providers.TlbConstructorProvider

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
            cellBuilder.storeBitString(value.bits)
        }

        override fun loadTlb(cellSlice: CellSlice): SnakeDataTail =
            SnakeDataTail(cellSlice.loadBitString(cellSlice.bits.size - cellSlice.bitsPosition))
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
            cellBuilder.storeBitString(value.bits)
            cellBuilder.storeRef {
                storeTlb(SnakeData, value.next)
            }
        }

        override fun loadTlb(cellSlice: CellSlice) =
            SnakeDataCons(
                cellSlice.loadBitString(cellSlice.bits.size - cellSlice.bitsPosition),
                cellSlice.loadRef {
                    loadTlb(SnakeData)
                }
            )
    }
}

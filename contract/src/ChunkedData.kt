package org.ton.kotlin.contract

import org.ton.kotlin.cell.Cell
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.hashmap.HashMapE
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.constructor.tlbCodec
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb

public data class ChunkedData(
    val data: HashMapE<SnakeDataTail>
) {
    public companion object : TlbConstructorProvider<ChunkedData> by ChunkedDataConstructor
}

private object ChunkedDataConstructor : TlbConstructor<ChunkedData>(
    schema = "chunked_data#_ data:(HashMapE 32 ^(SnakeData ~0)) = ChunkedData;"
) {
    // SnakeData ~0  is SnakeDataTail
    private val dataCodec =
        HashMapE.tlbCodec(32, Cell.tlbCodec(SnakeDataTail))

    override fun storeTlb(builder: CellBuilder, value: ChunkedData) {
        builder.storeTlb(dataCodec, value.data)
    }

    override fun loadTlb(slice: CellSlice): ChunkedData =
        ChunkedData(slice.loadTlb(dataCodec))
}

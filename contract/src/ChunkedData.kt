package org.ton.contract

import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.hashmap.HashMapE
import org.ton.tlb.TlbConstructor
import org.ton.tlb.constructor.tlbCodec
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb

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

package org.ton.kotlin.block

import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider

public object ChunkRefEmpty : TextChunkRef, TlbConstructorProvider<ChunkRefEmpty> by ChunkRefEmptyTlbConstructor

private object ChunkRefEmptyTlbConstructor : TlbConstructor<ChunkRefEmpty>(
    schema = "chunk_ref_empty\$_ = TextChunkRef 0;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: ChunkRefEmpty) {
    }

    override fun loadTlb(cellSlice: CellSlice): ChunkRefEmpty = ChunkRefEmpty
}

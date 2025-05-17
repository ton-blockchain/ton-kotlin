package org.ton.kotlin.block

import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider

public object TextChunkEmpty : TextChunks, TlbConstructorProvider<TextChunkEmpty> by TextChunkEmptyTlbConstructor

private object TextChunkEmptyTlbConstructor : TlbConstructor<TextChunkEmpty>(
    schema = "chunk_empty\$_ = TextChunks 0;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: TextChunkEmpty) {
    }

    override fun loadTlb(cellSlice: CellSlice): TextChunkEmpty = TextChunkEmpty
}

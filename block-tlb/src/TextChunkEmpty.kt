package org.ton.block

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider

public object TextChunkEmpty : TextChunks, TlbConstructorProvider<TextChunkEmpty> by TextChunkEmptyTlbConstructor

private object TextChunkEmptyTlbConstructor : TlbConstructor<TextChunkEmpty>(
    schema = "chunk_empty\$_ = TextChunks 0;"
) {
    override fun storeTlb(builder: CellBuilder, value: TextChunkEmpty) {
    }

    override fun loadTlb(slice: CellSlice): TextChunkEmpty = TextChunkEmpty
}

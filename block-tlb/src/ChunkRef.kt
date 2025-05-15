package org.ton.kotlin.block

import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.loadRef
import org.ton.kotlin.cell.storeRef
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.storeTlb

public data class ChunkRef(
    val ref: TextChunks
) : TextChunkRef {
    public companion object {
        public fun tlbConstructor(n: Int): TlbConstructor<ChunkRef> = ChunkRefTlbConstructor(n)
    }
}

private class ChunkRefTlbConstructor(
    n: Int
) : TlbConstructor<ChunkRef>(
    schema = "chunk_ref\$_ {n:#} ref:^(TextChunks (n + 1)) = TextChunkRef (n + 1);\n"
) {
    val n = n - 1

    override fun storeTlb(cellBuilder: CellBuilder, value: ChunkRef) {
        cellBuilder.storeRef {
            storeTlb(TextChunks.tlbCodec(n + 1), value.ref)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): ChunkRef {
        val ref = cellSlice.loadRef {
            loadTlb(TextChunks.tlbCodec(n + 1))
        }
        return ChunkRef(ref)
    }
}

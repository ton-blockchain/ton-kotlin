package org.ton.block

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.loadRef
import org.ton.cell.storeRef
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

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

    override fun storeTlb(builder: CellBuilder, value: ChunkRef) {
        builder.storeRef {
            storeTlb(TextChunks.tlbCodec(n + 1), value.ref)
        }
    }

    override fun loadTlb(slice: CellSlice): ChunkRef {
        val ref = slice.loadRef {
            loadTlb(TextChunks.tlbCodec(n + 1))
        }
        return ChunkRef(ref)
    }
}

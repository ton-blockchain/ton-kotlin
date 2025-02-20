package org.ton.block

import org.ton.bitstring.BitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

public class TextChunk(
    public val len: UByte,
    public val data: BitString,
    public val next: TextChunkRef
) : TextChunks {
    public companion object {
        public fun tlbConstructor(n: Int): TlbConstructor<TextChunk> = TextChunkTlbConstructor(n)
    }
}

private class TextChunkTlbConstructor(
    n: Int
) : TlbConstructor<TextChunk>(
    schema = "text_chunk\$_ {n:#} len:(## 8) data:(bits (len * 8)) next:(TextChunkRef n) = TextChunks (n + 1);"
) {
    val next = TextChunkRef.tlbCombinator(n - 1)

    override fun storeTlb(builder: CellBuilder, value: TextChunk) {
        builder.storeUInt8(value.len)
        builder.storeBitString(value.data)
        builder.storeTlb(next, value.next)
    }

    override fun loadTlb(slice: CellSlice): TextChunk {
        val len = slice.loadUInt8()
        val data = slice.loadBitString(len.toInt() * 8)
        val next = slice.loadTlb(next)
        return TextChunk(len, data, next)
    }
}

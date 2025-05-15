package org.ton.kotlin.block

import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.storeTlb

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

    override fun storeTlb(cellBuilder: CellBuilder, value: TextChunk) {
        cellBuilder.storeUInt8(value.len)
        cellBuilder.storeBits(value.data)
        cellBuilder.storeTlb(next, value.next)
    }

    override fun loadTlb(cellSlice: CellSlice): TextChunk {
        val len = cellSlice.loadUInt8()
        val data = cellSlice.loadBits(len.toInt() * 8)
        val next = cellSlice.loadTlb(next)
        return TextChunk(len, data, next)
    }
}

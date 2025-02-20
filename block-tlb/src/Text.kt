package org.ton.block

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb


public data class Text(
    val chunks: UByte,
    val rest: TextChunks
) {
    public companion object : TlbConstructorProvider<Text> by TextTlbConstructor
}

private object TextTlbConstructor : TlbConstructor<Text>(
    schema = "text\$_ chunks:(## 8) rest:(TextChunks chunks) = Text;"
) {
    override fun storeTlb(builder: CellBuilder, value: Text) {
        builder.storeUInt8(value.chunks)
        builder.storeTlb(TextChunks.tlbCodec(value.chunks.toInt()), value.rest)
    }

    override fun loadTlb(slice: CellSlice): Text {
        val chunks = slice.loadUInt8()
        val rest = slice.loadTlb(TextChunks.tlbCodec(chunks.toInt()))
        return Text(chunks, rest)
    }
}

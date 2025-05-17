package org.ton.kotlin.block

import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb


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

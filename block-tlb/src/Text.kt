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
    override fun storeTlb(cellBuilder: CellBuilder, value: Text) {
        cellBuilder.storeUInt8(value.chunks)
        cellBuilder.storeTlb(TextChunks.tlbCodec(value.chunks.toInt()), value.rest)
    }

    override fun loadTlb(cellSlice: CellSlice): Text {
        val chunks = cellSlice.loadUInt8()
        val rest = cellSlice.loadTlb(TextChunks.tlbCodec(chunks.toInt()))
        return Text(chunks, rest)
    }
}

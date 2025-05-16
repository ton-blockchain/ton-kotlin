package org.ton.kotlin.contract

import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbCombinator
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider
import org.ton.kotlin.tlb.storeTlb

public sealed interface ContentData {
    public data class Snake(val data: SnakeData) : ContentData

    public data class Chunks(val data: ChunkedData) : ContentData

    public companion object : TlbCombinatorProvider<ContentData> by ContentDataCombinator
}

private object ContentDataCombinator : TlbCombinator<ContentData>(
    ContentData::class,
    ContentData.Snake::class to ContentDataSnakeConstructor,
    ContentData.Chunks::class to ContentDataChunksConstructor
)

private object ContentDataSnakeConstructor : TlbConstructor<ContentData.Snake>(
    schema = "snake#00 data:(SnakeData ~n) = ContentData;"
) {
    override fun storeTlb(builder: CellBuilder, value: ContentData.Snake) {
        builder.storeTlb(
            SnakeData,
            value.data
        )
    }

    override fun loadTlb(slice: CellSlice): ContentData.Snake =
        ContentData.Snake(slice.loadTlb(SnakeData))
}

private object ContentDataChunksConstructor :
    TlbConstructor<ContentData.Chunks>(
        schema = "chunks#01 data:ChunkedData = ContentData;"
    ) {
    override fun storeTlb(builder: CellBuilder, value: ContentData.Chunks) {
        builder.storeTlb(ChunkedData, value.data)
    }

    override fun loadTlb(slice: CellSlice): ContentData.Chunks =
        ContentData.Chunks(slice.loadTlb(ChunkedData))
}

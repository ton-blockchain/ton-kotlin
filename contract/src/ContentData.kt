package org.ton.contract

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbCombinator
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbCombinatorProvider
import org.ton.tlb.storeTlb

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

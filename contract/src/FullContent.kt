package org.ton.kotlin.contract

import org.ton.kotlin.cell.Cell
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.hashmap.HashMapE
import org.ton.kotlin.tlb.TlbCombinator
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.constructor.tlbCodec
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider
import org.ton.kotlin.tlb.storeTlb

public sealed interface FullContent {
    public data class OnChain(
        val data: HashMapE<ContentData>
    ) : FullContent

    public data class OffChain(
        val uri: Text
    ) : FullContent

    public companion object : TlbCombinatorProvider<FullContent> by FullContentCombinator
}

private object FullContentCombinator : TlbCombinator<FullContent>(
    FullContent::class,
    FullContent.OnChain::class to FullContentOnChainConstructor,
    FullContent.OffChain::class to FullContentOffChainConstructor
)

private object FullContentOnChainConstructor : TlbConstructor<FullContent.OnChain>(
    schema = "onchain#00 data:(HashMapE 256 ^ContentData) = FullContent;"
) {
    val dataCodec = HashMapE.tlbCodec(256, Cell.tlbCodec(ContentData))

    override fun storeTlb(cellBuilder: CellBuilder, value: FullContent.OnChain) {
        cellBuilder.storeTlb(dataCodec, value.data)
    }

    override fun loadTlb(cellSlice: CellSlice): FullContent.OnChain =
        FullContent.OnChain(cellSlice.loadTlb(dataCodec))
}


private object FullContentOffChainConstructor : TlbConstructor<FullContent.OffChain>(
    schema = "offchain#01 uri:Text = FullContent;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: FullContent.OffChain) {
        cellBuilder.storeTlb(Text, value.uri)
    }

    override fun loadTlb(cellSlice: CellSlice): FullContent.OffChain =
        FullContent.OffChain(cellSlice.loadTlb(Text))
}

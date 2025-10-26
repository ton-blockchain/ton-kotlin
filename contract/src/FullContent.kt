package org.ton.contract

import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.hashmap.HashMapE
import org.ton.tlb.TlbCombinator
import org.ton.tlb.TlbConstructor
import org.ton.tlb.constructor.tlbCodec
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbCombinatorProvider
import org.ton.tlb.storeTlb

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

    override fun storeTlb(builder: CellBuilder, value: FullContent.OnChain) {
        builder.storeTlb(dataCodec, value.data)
    }

    override fun loadTlb(slice: CellSlice): FullContent.OnChain =
        FullContent.OnChain(slice.loadTlb(dataCodec))
}


private object FullContentOffChainConstructor : TlbConstructor<FullContent.OffChain>(
    schema = "offchain#01 uri:Text = FullContent;"
) {
    override fun storeTlb(builder: CellBuilder, value: FullContent.OffChain) {
        builder.storeTlb(Text, value.uri)
    }

    override fun loadTlb(slice: CellSlice): FullContent.OffChain =
        FullContent.OffChain(slice.loadTlb(Text))
}

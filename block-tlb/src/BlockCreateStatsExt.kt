package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.hashmap.HashmapAugE
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbPrettyPrinter
import org.ton.kotlin.tlb.constructor.tlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


@SerialName("block_create_stats_ext")
public data class BlockCreateStatsExt(
    val counters: HashmapAugE<CreatorStats, UInt>
) : BlockCreateStats {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("block_create_stats_ext") {
        field("counters", counters)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<BlockCreateStatsExt> by BlockCreateStateExtTlbConstructor
}

private object BlockCreateStateExtTlbConstructor : TlbConstructor<BlockCreateStatsExt>(
    schema = "block_create_stats_ext#34 counters:(HashmapAugE 256 CreatorStats uint32) = BlockCreateStats;"
) {
    val counters = HashmapAugE.tlbCodec(256, CreatorStats, UInt.tlbConstructor())

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: BlockCreateStatsExt
    ) = cellBuilder {
        storeTlb(counters, value.counters)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): BlockCreateStatsExt = cellSlice {
        val counters = loadTlb(counters)
        BlockCreateStatsExt(counters)
    }
}

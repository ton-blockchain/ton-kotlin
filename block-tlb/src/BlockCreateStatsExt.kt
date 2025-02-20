@file:Suppress("DEPRECATION")

package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.hashmap.HashmapAugE
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbPrettyPrinter
import org.ton.tlb.constructor.tlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb


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
        builder: CellBuilder,
        value: BlockCreateStatsExt
    ) = builder {
        storeTlb(counters, value.counters)
    }

    override fun loadTlb(
        slice: CellSlice
    ): BlockCreateStatsExt = slice {
        val counters = loadTlb(counters)
        BlockCreateStatsExt(counters)
    }
}

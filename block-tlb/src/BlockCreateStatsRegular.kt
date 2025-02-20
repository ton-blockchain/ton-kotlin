package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.hashmap.HashMapE
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbPrettyPrinter
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb


@SerialName("block_create_stats")
public data class BlockCreateStatsRegular(
    val counters: HashMapE<CreatorStats>
) : BlockCreateStats {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("block_create_stats") {
        field("counters", counters)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<BlockCreateStatsRegular> by BlockCreateStatsRegularTlbConstructor
}

private object BlockCreateStatsRegularTlbConstructor : TlbConstructor<BlockCreateStatsRegular>(
    schema = "block_create_stats#17 counters:(HashmapE 256 CreatorStats) = BlockCreateStats;"
) {
    val hashmapE = HashMapE.tlbCodec(256, CreatorStats)

    override fun storeTlb(
        builder: CellBuilder,
        value: BlockCreateStatsRegular
    ) = builder {
        storeTlb(hashmapE, value.counters)
    }

    override fun loadTlb(
        slice: CellSlice
    ): BlockCreateStatsRegular = slice {
        val counters = loadTlb(hashmapE)
        BlockCreateStatsRegular(counters)
    }
}

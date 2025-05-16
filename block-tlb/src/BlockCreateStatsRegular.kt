package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.hashmap.HashMapE
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbPrettyPrinter
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


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
        cellBuilder: CellBuilder,
        value: BlockCreateStatsRegular
    ) = cellBuilder {
        hashmapE.storeTlb(this, value.counters)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): BlockCreateStatsRegular = cellSlice {
        val counters = loadTlb(hashmapE)
        BlockCreateStatsRegular(counters)
    }
}

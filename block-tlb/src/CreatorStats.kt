package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import org.ton.tlb.providers.TlbConstructorProvider

@Serializable
@SerialName("creator_info")
public data class CreatorStats(
    @SerialName("mc_blocks") val mcBlocks: Counters,
    @SerialName("shard_blocks") val shardBlocks: Counters
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("creator_info") {
        field("mc_blocks", mcBlocks)
        field("shard_blocks", shardBlocks)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<CreatorStats> by CreatorStatsTlbConstructor
}

private object CreatorStatsTlbConstructor : TlbConstructor<CreatorStats>(
    schema = "creator_info#4 mc_blocks:Counters shard_blocks:Counters = CreatorStats;\n"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: CreatorStats
    ) = cellBuilder {
        storeTlb(Counters, value.mcBlocks)
        storeTlb(Counters, value.shardBlocks)
    }

    override fun loadTlb(cellSlice: CellSlice): CreatorStats = cellSlice {
        val mcBlocks = loadTlb(Counters)
        val shardBlocks = loadTlb(Counters)
        CreatorStats(mcBlocks, shardBlocks)
    }
}

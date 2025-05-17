package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider

@SerialName("block")

public data class Block(
    @SerialName("global_id")
    val globalId: Int, // global_id:int32
    val info: CellRef<BlockInfo>, // info : ^BlockInfo
    @SerialName("value_flow")
    val valueFlow: CellRef<ValueFlow>, // value_flow : ^ValueFlow
    @SerialName("state_update")
    val stateUpdate: CellRef<MerkleUpdate<ShardState>>, // state_update : ^(MERKLE_UPDATE ShardState)
    val extra: CellRef<BlockExtra> // extra : ^BlockExtra
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("block") {
            field("global_id", globalId)
            field("info", info)
            field("value_flow", valueFlow)
            field("state_update", stateUpdate)
            field("extra", extra)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbCombinatorProvider<Block> by TlbConstructor.asTlbCombinator()
}

private object TlbConstructor : org.ton.kotlin.tlb.TlbConstructor<Block>(
    schema = "block#11ef55aa global_id:int32 " +
            "info:^BlockInfo value_flow:^ValueFlow " +
            "state_update:^(MERKLE_UPDATE ShardState) " +
            "extra:^BlockExtra = Block;"
) {
    private val merkleUpdate = MerkleUpdate.tlbCodec(ShardState)

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: Block
    ) = cellBuilder {
        storeInt(value.globalId, 32)
        storeRef(value.info.toCell(BlockInfo))
        storeRef(value.valueFlow.toCell(ValueFlow))
        storeRef(value.stateUpdate.toCell(merkleUpdate))
        storeRef(value.extra.toCell(BlockExtra))
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): Block = cellSlice {
        val globalId = loadInt(32).toInt()
        val info = loadRef().asRef(BlockInfo)
        val valueFlow = loadRef().asRef(ValueFlow)
        val stateUpdate = loadRef().asRef(merkleUpdate)
        val extra = loadRef().asRef(BlockExtra)
        Block(globalId, info, valueFlow, stateUpdate, extra)
    }
}

package org.ton.block

import kotlinx.io.bytestring.ByteString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.parse
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.message.address.MsgAddressInt
import org.ton.kotlin.shard.ShardState
import org.ton.kotlin.transaction.Transaction
import org.ton.tlb.CellRef
import org.ton.tlb.TlbCodec
import org.ton.tlb.asRef

/**
 * Blockchain block.
 */
public data class Block(
    /**
     * Global network id.
     */
    val globalId: Int,

    /**
     * Block info.
     */
    val info: CellRef<BlockInfo>,

    /**
     * [CurrencyCollection] flow info.
     */
    val valueFlow: CellRef<ValueFlow>,

    /**
     * Merkle update for the shard state.
     */
    val stateUpdate: CellRef<MerkleUpdate<ShardState>>,

    /**
     * Block content.
     */
    val extra: CellRef<BlockExtra>
) {
    public fun loadInfo(context: CellContext = CellContext.EMPTY): BlockInfo = info.load(context)

    public fun loadValueFlow(context: CellContext = CellContext.EMPTY): ValueFlow = valueFlow.load(context)

    public fun loadStateUpdate(context: CellContext = CellContext.EMPTY): MerkleUpdate<ShardState> =
        stateUpdate.load(context)

    public fun loadExtra(context: CellContext = CellContext.EMPTY): BlockExtra = extra.load(context)

    public fun getTransaction(
        address: MsgAddressInt,
        lt: Long,
        context: CellContext = CellContext.EMPTY
    ): CellRef<Transaction>? {
        val info = loadInfo(context)
        if (lt !in info.startLt..info.endLt) {
            return null
        }
        val accountBlocks = loadExtra(context).loadAccountBlocks(context)
        val accountBlock = accountBlocks[ByteString(*address.address.toByteArray())]?.second ?: return null
        return accountBlock.transactions[lt]?.second
    }

    public companion object : TlbCodec<Block> by BlockTlbCodec
}

private object BlockTlbCodec : TlbCodec<Block> {
    private const val TAG = 0x11ef55aa
    private val merkleUpdate = MerkleUpdate.tlbCodec(ShardState)

    override fun storeTlb(
        builder: CellBuilder,
        value: Block,
        context: CellContext
    ) {
        builder.storeUInt(TAG, 32)
        builder.storeInt(value.globalId, 32)
        builder.storeRef(value.info.cell)
        builder.storeRef(value.stateUpdate.cell)
        builder.storeRef(value.extra.cell)
    }

    override fun loadTlb(
        slice: CellSlice,
        context: CellContext
    ): Block = slice.parse {
        val tag = loadUInt(32).toInt()
        require(tag == TAG) { "Invalid Block tag: ${tag.toHexString()}, expected: ${TAG.toHexString()}" }

        val globalId = loadInt(32).toInt()
        val info = loadRef().asRef(BlockInfo)
        val valueFlow = loadRef().asRef(ValueFlow)
        val stateUpdate = loadRef().asRef(merkleUpdate)
        val extra = loadRef().asRef(BlockExtra)

        Block(globalId, info, valueFlow, stateUpdate, extra)
    }
}

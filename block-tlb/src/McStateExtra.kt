package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.bigint.toInt
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbCombinatorProvider
import org.ton.tlb.providers.TlbConstructorProvider


@SerialName("masterchain_state_extra")
public data class McStateExtra(
    @SerialName("shard_hashes") val shardHashes: ShardHashes, // shard_hashes: ShardHashes
    val config: ConfigParams, // config: ConfigParams
    val r1: CellRef<McStateExtraAux>, // ^[$_ flags:(## 16) {<= flags 1} validator_info:ValidatorInfo prev_blocks:OldMcBlocksInfo after_key_block:Bool last_key_block:(Maybe ExtBlkRef) block_create_stats:flags.0?BlockCreateStats ]
    @SerialName("global_balance") val globalBalance: CurrencyCollection // global_balance: CurrencyCollection
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer.type("masterchain_state_extra") {
            field("shard_hashes", shardHashes)
            field("config", config)
            field(r1)
            field("global_balance", globalBalance)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbCombinatorProvider<McStateExtra> by McStateExtraTlbConstructor.asTlbCombinator()
}


public data class McStateExtraAux(
    val flags: Int, // flags : ## 16
    @SerialName("validator_info") val validatorInfo: ValidatorInfo, // validator_info : ValidatorInfo
    @SerialName("prev_blocks") val prevBlocks: OldMcBlocksInfo, // prev_blocks : OldMcBlocksInfo
    val afterKeyBlock: Boolean, // after_key_block : Bool
    val lastKeyBlock: Maybe<ExtBlkRef>, // last_key_block : Maybe ExtBlkRef
    val blockCreateStats: BlockCreateStats? // block_create_stats : flags.0?BlockCreateStats
) : TlbObject {
    init {
        check(flags <= 1) { "Invalid flags: $flags" }
    }

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer.type {
            field("flags", flags)
            field("validator_info", validatorInfo)
            field("prev_blocks", prevBlocks)
            field("after_key_block", afterKeyBlock)
            field("last_key_block", lastKeyBlock)
            field("block_create_stats", blockCreateStats)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<McStateExtraAux> by McStateExtraAuxTlbConstructor
}

private object McStateExtraTlbConstructor : TlbConstructor<McStateExtra>(
    schema = "masterchain_state_extra#cc26" +
            "  shard_hashes:ShardHashes" +
            "  config:ConfigParams" +
            "  ^[ flags:(## 16) { flags <= 1 }" +
            "     validator_info:ValidatorInfo" +
            "     prev_blocks:OldMcBlocksInfo" +
            "     after_key_block:Bool" +
            "     last_key_block:(Maybe ExtBlkRef)" +
            "     block_create_stats:(flags . 0)?BlockCreateStats ]" +
            "  global_balance:CurrencyCollection" +
            "= McStateExtra;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: McStateExtra
    ) = cellBuilder {
        storeTlb(ShardHashes, value.shardHashes)
        storeTlb(ConfigParams, value.config)
        storeRef(McStateExtraAux, value.r1)
        storeTlb(CurrencyCollection, value.globalBalance)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): McStateExtra = cellSlice {
        val shardHashes = loadTlb(ShardHashes)
        val config = loadTlb(ConfigParams)
        val r1 = loadRef(McStateExtraAux)
        val globalBalance = loadTlb(CurrencyCollection)
        McStateExtra(shardHashes, config, r1, globalBalance)
    }
}

private object McStateExtraAuxTlbConstructor : TlbConstructor<McStateExtraAux>(
    schema = "[\$_ flags:(## 16) {<= flags 1} " +
            "validator_info:ValidatorInfo " +
            "prev_blocks:OldMcBlocksInfo " +
            "after_key_block:Bool " +
            "last_key_block:(Maybe ExtBlkRef) " +
            "block_create_stats:flags.0?BlockCreateStats ]"
) {
    private val maybeExtBlkRef = Maybe.tlbCodec(ExtBlkRef)

    override fun loadTlb(cellSlice: CellSlice): McStateExtraAux {
        val flags = cellSlice.loadUInt(16).toInt()
        val validatorInfo = cellSlice.loadTlb(ValidatorInfo)
        val prevBlocks = cellSlice.loadTlb(OldMcBlocksInfo)
        val afterKeyBlock = cellSlice.loadBit()
        val lastKeyBlock = cellSlice.loadTlb(maybeExtBlkRef)
        val blockCreateStats = if (flags and 1 != 0) {
            cellSlice.loadTlb(BlockCreateStats)
        } else {
            null
        }
        return McStateExtraAux(flags, validatorInfo, prevBlocks, afterKeyBlock, lastKeyBlock, blockCreateStats)
    }

    override fun storeTlb(cellBuilder: CellBuilder, value: McStateExtraAux) = cellBuilder {
        storeUInt(value.flags, 16)
        storeTlb(ValidatorInfo, value.validatorInfo)
        storeTlb(OldMcBlocksInfo, value.prevBlocks)
        storeBit(value.afterKeyBlock)
        storeTlb(maybeExtBlkRef, value.lastKeyBlock)
        if (value.flags and 1 != 0) {
            storeTlb(BlockCreateStats, value.blockCreateStats!!)
        }
    }
}

package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.hashmap.HashMapE
import org.ton.hashmap.HashmapAugE
import org.ton.tlb.*
import org.ton.tlb.constructor.tlbCodec
import org.ton.tlb.providers.TlbCombinatorProvider
import org.ton.tlb.providers.TlbConstructorProvider


@SerialName("masterchain_block_extra")
public data class McBlockExtra(
    @SerialName("key_block") val keyBlock: Boolean,
    @SerialName("shard_hases") val shardHashes: HashMapE<BinTree<ShardDescr>>,
    @SerialName("shard_fees") val shardFees: HashmapAugE<ShardFeeCreated, ShardFeeCreated>,
    val r1: CellRef<McBlockExtraAux>,
    val config: ConfigParams?
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("masterchain_block_extra") {
            field("key_block", keyBlock)
            field("shard_hashes", shardHashes)
            field("shard_fees", shardFees)
            field(r1)
            field("config", config)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbCombinatorProvider<McBlockExtra> by McBlockExtraTlbConstructor.asTlbCombinator()
}


public data class McBlockExtraAux(
    @SerialName("prev_blk_signatures") val prevBlkSignatures: HashMapE<CryptoSignaturePair>,
    @SerialName("recover_create_msg") val recoverCreateMsg: Maybe<CellRef<InMsg>>,
    @SerialName("mint_msg") val mintMsg: Maybe<CellRef<InMsg>>,
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type {
            field("prev_blk_signatures", prevBlkSignatures)
            field("recover_create_msg", recoverCreateMsg)
            field("mint_msg", mintMsg)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<McBlockExtraAux> by McBlockExtraAuxTlbConstructor
}

private object McBlockExtraTlbConstructor : TlbConstructor<McBlockExtra>(
    schema = "masterchain_block_extra#cca5 " +
            "key_block:(## 1) " +
            "shard_hashes:ShardHashes " +
            "shard_fees:ShardFees " +
            "^[ prev_blk_signatures:(HashmapE 16 CryptoSignaturePair) " +
            "   recover_create_msg:(Maybe ^InMsg) " +
            "   mint_msg:(Maybe ^InMsg) ] " +
            "config:key_block?ConfigParams " +
            "= McBlockExtra;"
) {
    val shardHashes = HashMapE.tlbCodec(32, Cell.tlbCodec(BinTree.tlbCodec(ShardDescr)))
    val shardFees = HashmapAugE.tlbCodec(96, ShardFeeCreated, ShardFeeCreated)

    override fun storeTlb(
        builder: CellBuilder,
        value: McBlockExtra
    ) = builder {
        storeBoolean(value.keyBlock)
        storeTlb(shardHashes, value.shardHashes)
        storeTlb(shardFees, value.shardFees)
        storeRef(McBlockExtraAux, value.r1)
        if (value.keyBlock && value.config != null) {
            storeTlb(ConfigParams, value.config)
        }
    }

    override fun loadTlb(
        slice: CellSlice
    ): McBlockExtra = slice {
        val keyBlock = loadBoolean()
        val shardHashes = loadTlb(shardHashes)
        val shardFees = loadTlb(shardFees)
        val config = if (keyBlock) loadTlb(ConfigParams) else null
        val r1 = loadRef(McBlockExtraAux)
        McBlockExtra(keyBlock, shardHashes, shardFees, r1, config)
    }
}

private object McBlockExtraAuxTlbConstructor : TlbConstructor<McBlockExtraAux>(
    schema = "\$_ prev_blk_signatures:(HashmapE 16 CryptoSignaturePair) recover_create_msg:(Maybe ^InMsg) mint_msg:(Maybe ^InMsg)"
) {
    val HashMapE16CryptoSignaturePair = HashMapE.tlbCodec(16, CryptoSignaturePair)
    val MaybeInMsg = Maybe.tlbCodec(CellRef.tlbCodec(InMsg))

    override fun loadTlb(slice: CellSlice): McBlockExtraAux {
        val prevBlkSignatures = slice.loadTlb(HashMapE16CryptoSignaturePair)
        val recoverCreateMsg = slice.loadTlb(MaybeInMsg)
        val mintMsg = slice.loadTlb(MaybeInMsg)
        return McBlockExtraAux(prevBlkSignatures, recoverCreateMsg, mintMsg)
    }

    override fun storeTlb(builder: CellBuilder, value: McBlockExtraAux) {
        builder {
            storeTlb(HashMapE16CryptoSignaturePair, value.prevBlkSignatures)
            storeTlb(MaybeInMsg, value.recoverCreateMsg)
            storeTlb(MaybeInMsg, value.mintMsg)
        }
    }
}

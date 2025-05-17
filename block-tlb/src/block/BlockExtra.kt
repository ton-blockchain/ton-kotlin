package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.hashmap.HashmapAugE
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor

@SerialName("block_extra")

public data class BlockExtra(
    @SerialName("in_msg_descr") val inMsgDescr: CellRef<HashmapAugE<InMsg, ImportFees>>,
    @SerialName("out_msg_descr") val outMsgDescr: CellRef<HashmapAugE<OutMsg, CurrencyCollection>>,
    @SerialName("account_blocks") val accountBlocks: CellRef<HashmapAugE<AccountBlock, CurrencyCollection>>,
    @SerialName("rand_seed") val randSeed: BitString,
    @SerialName("created_by") val createdBy: BitString,
    val custom: Maybe<CellRef<McBlockExtra>>
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer.type("block_extra") {
            field("in_msg_descr", inMsgDescr)
            field("out_msg_descr", outMsgDescr)
            field("account_blocks", accountBlocks)
            field("rand_seed", randSeed)
            field("created_by", createdBy)
            field("custom", custom)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbCodec<BlockExtra> by BlockExtraTlbConstructor.asTlbCombinator()
}

private object BlockExtraTlbConstructor : TlbConstructor<BlockExtra>(
    schema = "block_extra#4a33f6fd in_msg_descr:^InMsgDescr\n" +
            "  out_msg_descr:^OutMsgDescr\n" +
            "  account_blocks:^ShardAccountBlocks\n" +
            "  rand_seed:bits256\n" +
            "  created_by:bits256\n" +
            "  custom:(Maybe ^McBlockExtra) = BlockExtra;"
) {
    val inMsgDescr = CellRef.tlbCodec(HashmapAugE.tlbCodec(256, InMsg, ImportFees))
    val outMsgDescr = CellRef.tlbCodec(HashmapAugE.tlbCodec(256, OutMsg, CurrencyCollection))
    val shardAccountBlock = CellRef.tlbCodec(
        HashmapAugE.tlbCodec(
            256,
            AccountBlock,
            CurrencyCollection
        )
    )
    val maybeMcBlockExtra = Maybe.tlbCodec(CellRef.tlbCodec(McBlockExtra))

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: BlockExtra
    ) = cellBuilder {
        storeTlb(inMsgDescr, value.inMsgDescr)
        storeTlb(outMsgDescr, value.outMsgDescr)
        storeTlb(shardAccountBlock, value.accountBlocks)
        storeBitString(value.randSeed)
        storeBitString(value.createdBy)
        storeTlb(maybeMcBlockExtra, value.custom)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): BlockExtra = cellSlice {
        val inMsgDescr = cellSlice.loadTlb(inMsgDescr)
        val outMsgDescr = cellSlice.loadTlb(outMsgDescr)
        val accountBlocks = loadTlb(shardAccountBlock)
        val randSeed = loadBitString(256)
        val createdBy = loadBitString(256)
        val custom = loadTlb(maybeMcBlockExtra)
        BlockExtra(inMsgDescr, outMsgDescr, accountBlocks, randSeed, createdBy, custom)
    }
}

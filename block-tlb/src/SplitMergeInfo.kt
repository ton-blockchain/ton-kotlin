package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.bigint.toInt
import org.ton.bitstring.BitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbObject
import org.ton.tlb.TlbPrettyPrinter


@SerialName("split_merge_info")
public data class SplitMergeInfo(
    val curShardPfxLen: Int,
    val accSplitDepth: Int,
    val thisAddr: BitString,
    val siblingAddr: BitString
) : TlbObject {
    init {
        require(thisAddr.size == 256)
        require(siblingAddr.size == 256)
    }

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("split_merge_info") {
            field("cur_shard_pfx_len", curShardPfxLen)
            field("acc_split_depth", accSplitDepth)
            field("this_addr", thisAddr)
            field("sibling_addr", siblingAddr)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbCodec<SplitMergeInfo> by SplitMergeInfoTlbConstructor
}

private object SplitMergeInfoTlbConstructor : TlbConstructor<SplitMergeInfo>(
    schema = "split_merge_info\$_ cur_shard_pfx_len:(## 6)\n" +
            "  acc_split_depth:(## 6) this_addr:bits256 sibling_addr:bits256\n" +
            "  = SplitMergeInfo;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: SplitMergeInfo
    ) = cellBuilder {
        storeUInt(value.curShardPfxLen, 6)
        storeUInt(value.accSplitDepth, 6)
        storeBits(value.thisAddr)
        storeBits(value.siblingAddr)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): SplitMergeInfo = cellSlice {
        val curShardPfxLen = loadUInt(6).toInt()
        val accSplitDepth = loadUInt(6).toInt()
        val thisAddr = loadBits(256)
        val siblingAddr = loadBits(256)
        SplitMergeInfo(curShardPfxLen, accSplitDepth, thisAddr, siblingAddr)
    }
}

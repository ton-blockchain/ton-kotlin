package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.sdk.bigint.toInt
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor

@SerialName("block_info")

public data class BlockInfo(
    val version: UInt, // version : uint32
    @SerialName("not_master") val notMaster: Boolean, // not_master : ## 1
    @SerialName("after_merge") val afterMerge: Boolean, // after_merge : ## 1
    @SerialName("before_split") val beforeSplit: Boolean, // before_split : ## 1
    @SerialName("after_split") val afterSplit: Boolean, // after_split : ## 1
    @SerialName("want_split") val wantSplit: Boolean, // want_split : Bool
    @SerialName("want_merge") val wantMerge: Boolean, // want_merge : Bool
    @SerialName("key_block") val keyBlock: Boolean, // key_block : Bool
    @SerialName("ver_seqno_inc") val vertSeqnoIncr: Boolean, // vert_seqno_incr : ## 1
    val flags: Int, // flags : ## 8
    @SerialName("seq_no") val seqNo: Int, // seq_no : #
    @SerialName("vert_seq_no") val vertSeqNo: Int, // vert_seq_no : #
    val shard: ShardIdent, // shard : ShardIdent
    @SerialName("gen_utime") val genUtime: UInt, // gen_utime : uint32
    @SerialName("start_lt") val startLt: ULong, // start_lt : uint64
    @SerialName("end_lt") val endLt: ULong, // end_lt : uint64
    @SerialName("gen_validator_list_hash_short") val genValidatorListHashShort: UInt, // gen_validator_list_hash_short : uint32
    @SerialName("gen_catchain_seqno") val genCatchainSeqno: UInt, // gen_catchain_seqno : uint32
    @SerialName("min_ref_mc_seqno") val minRefMcSeqno: UInt, // min_ref_mc_seqno : uint32
    @SerialName("prevKeyBlockSeqno") val prevKeyBlockSeqno: UInt, // prev_key_block_seqno : uint32
    @SerialName("gen_software") val genSoftware: GlobalVersion?, // gen_software : flags.0?GlobalVersion
    @SerialName("master_ref") val masterRef: CellRef<BlkMasterInfo>?, // master_ref : not_master?^BlkMasterInfo
    @SerialName("prev_ref") val prevRef: CellRef<BlkPrevInfo>, // prev_ref : ^(BlkPrevInfo after_merge)
    @SerialName("prev_vert_ref") val prevVertRef: CellRef<BlkPrevInfo>? // prev_vert_ref : after_merge?^(BlkPrevInfo 0)
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer.type("block_info") {
            field("version", version)
            field("not_master", notMaster)
            field("after_merge", afterMerge)
            field("before_split", beforeSplit)
            field("after_split", afterSplit)
            field("want_split", wantSplit)
            field("want_merge", wantMerge)
            field("key_block", keyBlock)
            field("ver_seqno_inc", vertSeqnoIncr)
            field("flags", flags)
            field("seq_no", seqNo)
            field("vert_seq_no", vertSeqNo)
            field("shard", shard)
            field("gen_utime", genUtime)
            field("start_lt", startLt)
            field("end_lt", endLt)
            field("gen_validator_list_hash_short", genValidatorListHashShort)
            field("gen_catchain_seqno", genCatchainSeqno)
            field("min_ref_mc_seqno", minRefMcSeqno)
            field("prevKeyBlockSeqno", prevKeyBlockSeqno)
            field("gen_software", genSoftware)
            field("master_ref", masterRef)
            field("prev_ref", prevRef)
            field("prev_vert_ref", prevVertRef)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbCodec<BlockInfo> by BlockInfoTlbConstructor.asTlbCombinator()
}

private object BlockInfoTlbConstructor : TlbConstructor<BlockInfo>(
    schema = "block_info#9bc7a987 version:uint32 " +
            "not_master:(## 1) " +
            "after_merge:(## 1) before_split:(## 1) " +
            "after_split:(## 1) " +
            "want_split:Bool want_merge:Bool " +
            "key_block:Bool vert_seqno_incr:(## 1) " +
            "flags:(## 8) { flags <= 1 } " +
            "seq_no:# vert_seq_no:# { vert_seq_no >= vert_seqno_incr } " +
            "{ prev_seq_no:# } { ~prev_seq_no + 1 = seq_no } " +
            "shard:ShardIdent gen_utime:uint32 " +
            "start_lt:uint64 end_lt:uint64 " +
            "gen_validator_list_hash_short:uint32 " +
            "gen_catchain_seqno:uint32 " +
            "min_ref_mc_seqno:uint32 " +
            "prev_key_block_seqno:uint32 " +
            "gen_software:flags . 0?GlobalVersion " +
            "master_ref:not_master?^BlkMasterInfo " +
            "prev_ref:^(BlkPrevInfo after_merge) " +
            "prev_vert_ref:vert_seqno_incr?^(BlkPrevInfo 0) " +
            "= BlockInfo;"
) {
    private val blkMasterInfo = CellRef.tlbCodec(BlkMasterInfo)
    private val blkPrevInfoVert = CellRef.tlbCodec(BlkPrevInfo.tlbCodec(0))

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: BlockInfo
    ) = cellBuilder {
        storeUInt32(value.version)
        storeBit(value.notMaster)
        storeBit(value.afterMerge)
        storeBit(value.beforeSplit)
        storeBit(value.afterSplit)
        storeBit(value.wantSplit)
        storeBit(value.wantMerge)
        storeBit(value.keyBlock)
        storeBit(value.vertSeqnoIncr)
        storeInt(value.flags, 8)
        storeInt(value.seqNo, 32)
        storeInt(value.vertSeqNo, 32)
        storeTlb(ShardIdent, value.shard)
        storeUInt32(value.genUtime)
        storeUInt64(value.startLt)
        storeUInt64(value.endLt)
        storeUInt32(value.genValidatorListHashShort)
        storeUInt32(value.genCatchainSeqno)
        storeUInt32(value.minRefMcSeqno)
        storeUInt32(value.prevKeyBlockSeqno)
        if (value.flags and 1 != 0 && value.genSoftware != null) {
            storeTlb(GlobalVersion, value.genSoftware)
        }
        if (value.notMaster && value.masterRef != null) {
            storeTlb(blkMasterInfo, value.masterRef)
        }
        storeTlb(CellRef.tlbCodec(BlkPrevInfo.tlbCodec(value.afterMerge)), value.prevRef)
        if (value.vertSeqnoIncr && value.prevVertRef != null) {
            storeTlb(blkPrevInfoVert, value.prevVertRef)
        }
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): BlockInfo = cellSlice {
        val version = loadUInt32()
        val notMaster = loadBit()
        val afterMerge = loadBit()
        val beforeSplit = loadBit()
        val afterSplit = loadBit()
        val wantSplit = loadBit()
        val wantMerge = loadBit()
        val keyBlock = loadBit()
        val verSeqnoIncr = loadBit()
        val flags = loadInt(8).toInt()
        val seqNo = loadUInt32().toInt()
        val vertSeqNo = loadUInt32().toInt()
        val shard = loadTlb(ShardIdent)
        val genUtime = loadUInt32()
        val startLt = loadUInt64()
        val endLt = loadUInt64()
        val genValidatorListHashShort = loadUInt32()
        val genCatchainSeqno = loadUInt32()
        val minRefMcSeqno = loadUInt32()
        val prevKeyBlockSeqno = loadUInt32()
        val genSoftware = if (flags and 1 != 0) loadTlb(GlobalVersion) else null
        val masterRef = if (notMaster) loadTlb(blkMasterInfo) else null
        val prevRef = loadTlb(CellRef.tlbCodec(BlkPrevInfo.tlbCodec(afterMerge)))
        val prevVertRef = if (verSeqnoIncr) loadTlb(blkPrevInfoVert) else null
        BlockInfo(
            version,
            notMaster,
            afterMerge,
            beforeSplit,
            afterSplit,
            wantSplit,
            wantMerge,
            keyBlock,
            verSeqnoIncr,
            flags,
            seqNo,
            vertSeqNo,
            shard,
            genUtime,
            startLt,
            endLt,
            genValidatorListHashShort,
            genCatchainSeqno,
            minRefMcSeqno,
            prevKeyBlockSeqno,
            genSoftware,
            masterRef,
            prevRef,
            prevVertRef
        )
    }
}

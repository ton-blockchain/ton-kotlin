package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.bitstring.BitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.*
import org.ton.tlb.providers.TlbCombinatorProvider
import org.ton.tlb.providers.TlbConstructorProvider


public sealed interface ShardDescr : TlbObject {
    public companion object : TlbCombinatorProvider<ShardDescr> by ShardDescrTlbCombinator
}

private object ShardDescrTlbCombinator : TlbCombinator<ShardDescr>(
    ShardDescr::class,
    ShardDescrOld::class to ShardDescrOld,
    ShardDescrNew::class to ShardDescrNew,
)


@SerialName("shard_descr_old")
public data class ShardDescrOld(
    @SerialName("seq_no") val seqNo: UInt,
    @SerialName("reg_mc_seqno") val regMcSeqno: UInt,
    @SerialName("start_lt") val startLt: ULong,
    @SerialName("end_lt") val endLt: ULong,
    @SerialName("root_hash") val rootHash: BitString,
    @SerialName("file_hash") val fileHash: BitString,
    @SerialName("before_split") val beforeSplit: Boolean,
    @SerialName("before_merge") val beforeMerge: Boolean,
    @SerialName("want_split") val wantSplit: Boolean,
    @SerialName("want_merge") val wantMerge: Boolean,
    @SerialName("nx_cc_updated") val nxCcUpdated: Boolean,
    val flags: Int,
    @SerialName("next_catchain_seqno") val nextCatchainSeqno: UInt,
    @SerialName("next_validator_shard") val nextValidatorShard: ULong,
    @SerialName("min_ref_mc_seqno") val minRefMcSeqno: UInt,
    @SerialName("gen_utime") val genUtime: UInt,
    @SerialName("split_merge_at") val splitMergeAt: FutureSplitMerge,
    @SerialName("fees_collected") val feesCollected: CurrencyCollection,
    @SerialName("funds_created") val fundsCreated: CurrencyCollection
) : ShardDescr {
    init {
        require(rootHash.size == 256) { "expected rootHash.size == 256, actual: ${rootHash.size}" }
        require(fileHash.size == 256) { "expected fileHash.size == 256, actual: ${fileHash.size}" }
    }

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("shard_descr_old") {
            field("seq_no", seqNo)
            field("reg_mc_seqno", regMcSeqno)
            field("start_lt", startLt)
            field("end_lt", endLt)
            field("root_hash", rootHash)
            field("file_hash", fileHash)
            field("before_split", beforeSplit)
            field("before_merge", beforeMerge)
            field("want_split", wantSplit)
            field("want_merge", wantMerge)
            field("nx_cc_updated", nxCcUpdated)
            field("flags", flags)
            field("next_catchain_seqno", nextCatchainSeqno)
            field("next_validator_shard", nextValidatorShard)
            field("min_ref_mc_seqno", minRefMcSeqno)
            field("gen_utime", genUtime)
            field("split_merge_at", splitMergeAt)
            field("fees_collected", feesCollected)
            field("funds_created", fundsCreated)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<ShardDescrOld> by ShardDescrOldTlbConstructor
}


public data class ShardDescrAux(
    @SerialName("fees_collected") val feesCollected: CurrencyCollection,
    @SerialName("funds_created") val fundsCreated: CurrencyCollection
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type {
            field("fees_collected", feesCollected)
            field("funds_created", fundsCreated)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<ShardDescrAux> by ShardDescrAuxTlbConstructor
}


public data class ShardDescrNew(
    @SerialName("seq_no") val seqNo: UInt,
    @SerialName("reg_mc_seqno") val regMcSeqno: UInt,
    @SerialName("start_lt") val startLt: ULong,
    @SerialName("end_lt") val endLt: ULong,
    @SerialName("root_hash") val rootHash: BitString,
    @SerialName("file_hash") val fileHash: BitString,
    @SerialName("before_split") val beforeSplit: Boolean,
    @SerialName("before_merge") val beforeMerge: Boolean,
    @SerialName("want_split") val wantSplit: Boolean,
    @SerialName("want_merge") val wantMerge: Boolean,
    @SerialName("nx_cc_updated") val nxCcUpdated: Boolean,
    val flags: Int,
    @SerialName("next_catchain_seqno") val nextCatchainSeqno: UInt,
    @SerialName("next_validator_shard") val nextValidatorShard: ULong,
    @SerialName("min_ref_mc_seqno") val minRefMcSeqno: UInt,
    @SerialName("gen_utime") val genUtime: UInt,
    @SerialName("split_merge_at") val splitMergeAt: FutureSplitMerge,
    val r1: CellRef<ShardDescrAux>
) : ShardDescr {
    init {
        require(rootHash.size == 256) { "expected rootHash.size == 256, actual: ${rootHash.size}" }
        require(fileHash.size == 256) { "expected fileHash.size == 256, actual: ${fileHash.size}" }
    }

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("shard_descr_new") {
            field("seq_no", seqNo)
            field("reg_mc_seqno", regMcSeqno)
            field("start_lt", startLt)
            field("end_lt", endLt)
            field("root_hash", rootHash)
            field("file_hash", fileHash)
            field("before_split", beforeSplit)
            field("before_merge", beforeMerge)
            field("want_split", wantSplit)
            field("want_merge", wantMerge)
            field("nx_cc_updated", nxCcUpdated)
            field("flags", flags)
            field("next_catchain_seqno", nextCatchainSeqno)
            field("next_validator_shard", nextValidatorShard)
            field("min_ref_mc_seqno", minRefMcSeqno)
            field("gen_utime", genUtime)
            field("split_merge_at", splitMergeAt)
            field(r1)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<ShardDescrNew> by ShardDescrNewTlbConstructor
}

private object ShardDescrOldTlbConstructor : TlbConstructor<ShardDescrOld>(
    schema = "shard_descr_old#b seq_no:uint32 reg_mc_seqno:uint32\n" +
            "  start_lt:uint64 end_lt:uint64\n" +
            "  root_hash:bits256 file_hash:bits256 \n" +
            "  before_split:Bool before_merge:Bool\n" +
            "  want_split:Bool want_merge:Bool\n" +
            "  nx_cc_updated:Bool flags:(## 3) { flags = 0 }\n" +
            "  next_catchain_seqno:uint32 next_validator_shard:uint64\n" +
            "  min_ref_mc_seqno:uint32 gen_utime:uint32\n" +
            "  split_merge_at:FutureSplitMerge\n" +
            "  fees_collected:CurrencyCollection\n" +
            "  funds_created:CurrencyCollection = ShardDescr;"
) {
    override fun loadTlb(slice: CellSlice): ShardDescrOld {
        val seqNo = slice.loadUInt32()
        val regMcSeqno = slice.loadUInt32()
        val startLt = slice.loadULong()
        val endLt = slice.loadULong()
        val rootHash = slice.loadBitString(256)
        val fileHash = slice.loadBitString(256)
        val beforeSplit = slice.loadBoolean()
        val beforeMerge = slice.loadBoolean()
        val wantSplit = slice.loadBoolean()
        val wantMerge = slice.loadBoolean()
        val nxCcUpdated = slice.loadBoolean()
        val flags = slice.loadInt(3).toInt()
        val nextCatchainSeqno = slice.loadUInt32()
        val nextValidatorShard = slice.loadULong()
        val minRefMcSeqno = slice.loadUInt32()
        val genUtime = slice.loadUInt32()
        val splitMergeAt = slice.loadTlb(FutureSplitMerge)
        val feesCollected = slice.loadTlb(CurrencyCollection)
        val fundsCreated = slice.loadTlb(CurrencyCollection)
        return ShardDescrOld(
            seqNo = seqNo,
            regMcSeqno = regMcSeqno,
            startLt = startLt,
            endLt = endLt,
            rootHash = rootHash,
            fileHash = fileHash,
            beforeSplit = beforeSplit,
            beforeMerge = beforeMerge,
            wantSplit = wantSplit,
            wantMerge = wantMerge,
            nxCcUpdated = nxCcUpdated,
            flags = flags,
            nextCatchainSeqno = nextCatchainSeqno,
            nextValidatorShard = nextValidatorShard,
            minRefMcSeqno = minRefMcSeqno,
            genUtime = genUtime,
            splitMergeAt = splitMergeAt,
            feesCollected = feesCollected,
            fundsCreated = fundsCreated
        )
    }

    override fun storeTlb(builder: CellBuilder, value: ShardDescrOld) {
        builder.storeUInt32(value.seqNo)
        builder.storeUInt32(value.regMcSeqno)
        builder.storeULong(value.startLt)
        builder.storeULong(value.endLt)
        builder.storeBitString(value.rootHash)
        builder.storeBitString(value.fileHash)
        builder.storeBoolean(value.beforeSplit)
        builder.storeBoolean(value.beforeMerge)
        builder.storeBoolean(value.wantSplit)
        builder.storeBoolean(value.wantMerge)
        builder.storeBoolean(value.nxCcUpdated)
        builder.storeInt(value.flags, 3)
        builder.storeUInt32(value.nextCatchainSeqno)
        builder.storeULong(value.nextValidatorShard)
        builder.storeUInt32(value.minRefMcSeqno)
        builder.storeUInt32(value.genUtime)
        builder.storeTlb(FutureSplitMerge, value.splitMergeAt)
        builder.storeTlb(CurrencyCollection, value.feesCollected)
        builder.storeTlb(CurrencyCollection, value.fundsCreated)
    }
}


private object ShardDescrAuxTlbConstructor : TlbConstructor<ShardDescrAux>(
    schema = ""
) {
    override fun loadTlb(slice: CellSlice): ShardDescrAux {
        val feesCollected = slice.loadTlb(CurrencyCollection)
        val fundsCreated = slice.loadTlb(CurrencyCollection)
        return ShardDescrAux(feesCollected, fundsCreated)
    }

    override fun storeTlb(builder: CellBuilder, value: ShardDescrAux) {
        builder.storeTlb(CurrencyCollection, value.feesCollected)
        builder.storeTlb(CurrencyCollection, value.fundsCreated)
    }
}

private object ShardDescrNewTlbConstructor : TlbConstructor<ShardDescrNew>(
    schema = "shard_descr_new#a seq_no:uint32 reg_mc_seqno:uint32\n" +
            "  start_lt:uint64 end_lt:uint64\n" +
            "  root_hash:bits256 file_hash:bits256 \n" +
            "  before_split:Bool before_merge:Bool\n" +
            "  want_split:Bool want_merge:Bool\n" +
            "  nx_cc_updated:Bool flags:(## 3) { flags = 0 }\n" +
            "  next_catchain_seqno:uint32 next_validator_shard:uint64\n" +
            "  min_ref_mc_seqno:uint32 gen_utime:uint32\n" +
            "  split_merge_at:FutureSplitMerge\n" +
            "  fees_collected:CurrencyCollection\n" +
            "  funds_created:CurrencyCollection = ShardDescr;"
) {
    override fun loadTlb(slice: CellSlice): ShardDescrNew {
        val seqNo = slice.loadUInt32()
        val regMcSeqno = slice.loadUInt32()
        val startLt = slice.loadULong()
        val endLt = slice.loadULong()
        val rootHash = slice.loadBitString(256)
        val fileHash = slice.loadBitString(256)
        val beforeSplit = slice.loadBoolean()
        val beforeMerge = slice.loadBoolean()
        val wantSplit = slice.loadBoolean()
        val wantMerge = slice.loadBoolean()
        val nxCcUpdated = slice.loadBoolean()
        val flags = slice.loadInt(3).toInt()
        val nextCatchainSeqno = slice.loadUInt32()
        val nextValidatorShard = slice.loadULong()
        val minRefMcSeqno = slice.loadUInt32()
        val genUtime = slice.loadUInt32()
        val splitMergeAt = slice.loadTlb(FutureSplitMerge)
        val r1 = slice.loadRef(ShardDescrAux)
        return ShardDescrNew(
            seqNo = seqNo,
            regMcSeqno = regMcSeqno,
            startLt = startLt,
            endLt = endLt,
            rootHash = rootHash,
            fileHash = fileHash,
            beforeSplit = beforeSplit,
            beforeMerge = beforeMerge,
            wantSplit = wantSplit,
            wantMerge = wantMerge,
            nxCcUpdated = nxCcUpdated,
            flags = flags,
            nextCatchainSeqno = nextCatchainSeqno,
            nextValidatorShard = nextValidatorShard,
            minRefMcSeqno = minRefMcSeqno,
            genUtime = genUtime,
            splitMergeAt = splitMergeAt,
            r1 = r1,
        )
    }

    override fun storeTlb(builder: CellBuilder, value: ShardDescrNew) {
        builder.storeUInt32(value.seqNo)
        builder.storeUInt32(value.regMcSeqno)
        builder.storeULong(value.startLt)
        builder.storeULong(value.endLt)
        builder.storeBitString(value.rootHash)
        builder.storeBitString(value.fileHash)
        builder.storeBoolean(value.beforeSplit)
        builder.storeBoolean(value.beforeMerge)
        builder.storeBoolean(value.wantSplit)
        builder.storeBoolean(value.wantMerge)
        builder.storeBoolean(value.nxCcUpdated)
        builder.storeInt(value.flags, 3)
        builder.storeUInt32(value.nextCatchainSeqno)
        builder.storeULong(value.nextValidatorShard)
        builder.storeUInt32(value.minRefMcSeqno)
        builder.storeUInt32(value.genUtime)
        builder.storeTlb(FutureSplitMerge, value.splitMergeAt)
        builder.storeRef(ShardDescrAux, value.r1)
    }
}

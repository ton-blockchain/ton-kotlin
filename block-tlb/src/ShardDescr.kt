package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.bitstring.BitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.sdk.bigint.toInt
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor
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
    override fun loadTlb(cellSlice: CellSlice): ShardDescrOld {
        val seqNo = cellSlice.loadUInt32()
        val regMcSeqno = cellSlice.loadUInt32()
        val startLt = cellSlice.loadUInt64()
        val endLt = cellSlice.loadUInt64()
        val rootHash = cellSlice.loadBits(256)
        val fileHash = cellSlice.loadBits(256)
        val beforeSplit = cellSlice.loadBit()
        val beforeMerge = cellSlice.loadBit()
        val wantSplit = cellSlice.loadBit()
        val wantMerge = cellSlice.loadBit()
        val nxCcUpdated = cellSlice.loadBit()
        val flags = cellSlice.loadInt(3).toInt()
        val nextCatchainSeqno = cellSlice.loadUInt32()
        val nextValidatorShard = cellSlice.loadUInt64()
        val minRefMcSeqno = cellSlice.loadUInt32()
        val genUtime = cellSlice.loadUInt32()
        val splitMergeAt = cellSlice.loadTlb(FutureSplitMerge)
        val feesCollected = cellSlice.loadTlb(CurrencyCollection)
        val fundsCreated = cellSlice.loadTlb(CurrencyCollection)
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

    override fun storeTlb(cellBuilder: CellBuilder, value: ShardDescrOld) {
        cellBuilder.storeUInt32(value.seqNo)
        cellBuilder.storeUInt32(value.regMcSeqno)
        cellBuilder.storeUInt64(value.startLt)
        cellBuilder.storeUInt64(value.endLt)
        cellBuilder.storeBits(value.rootHash)
        cellBuilder.storeBits(value.fileHash)
        cellBuilder.storeBit(value.beforeSplit)
        cellBuilder.storeBit(value.beforeMerge)
        cellBuilder.storeBit(value.wantSplit)
        cellBuilder.storeBit(value.wantMerge)
        cellBuilder.storeBit(value.nxCcUpdated)
        cellBuilder.storeInt(value.flags, 3)
        cellBuilder.storeUInt32(value.nextCatchainSeqno)
        cellBuilder.storeUInt64(value.nextValidatorShard)
        cellBuilder.storeUInt32(value.minRefMcSeqno)
        cellBuilder.storeUInt32(value.genUtime)
        cellBuilder.storeTlb(FutureSplitMerge, value.splitMergeAt)
        cellBuilder.storeTlb(CurrencyCollection, value.feesCollected)
        cellBuilder.storeTlb(CurrencyCollection, value.fundsCreated)
    }
}


private object ShardDescrAuxTlbConstructor : TlbConstructor<ShardDescrAux>(
    schema = ""
) {
    override fun loadTlb(cellSlice: CellSlice): ShardDescrAux {
        val feesCollected = cellSlice.loadTlb(CurrencyCollection)
        val fundsCreated = cellSlice.loadTlb(CurrencyCollection)
        return ShardDescrAux(feesCollected, fundsCreated)
    }

    override fun storeTlb(cellBuilder: CellBuilder, value: ShardDescrAux) {
        cellBuilder.storeTlb(CurrencyCollection, value.feesCollected)
        cellBuilder.storeTlb(CurrencyCollection, value.fundsCreated)
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
    override fun loadTlb(cellSlice: CellSlice): ShardDescrNew {
        val seqNo = cellSlice.loadUInt32()
        val regMcSeqno = cellSlice.loadUInt32()
        val startLt = cellSlice.loadUInt64()
        val endLt = cellSlice.loadUInt64()
        val rootHash = cellSlice.loadBits(256)
        val fileHash = cellSlice.loadBits(256)
        val beforeSplit = cellSlice.loadBit()
        val beforeMerge = cellSlice.loadBit()
        val wantSplit = cellSlice.loadBit()
        val wantMerge = cellSlice.loadBit()
        val nxCcUpdated = cellSlice.loadBit()
        val flags = cellSlice.loadInt(3).toInt()
        val nextCatchainSeqno = cellSlice.loadUInt32()
        val nextValidatorShard = cellSlice.loadUInt64()
        val minRefMcSeqno = cellSlice.loadUInt32()
        val genUtime = cellSlice.loadUInt32()
        val splitMergeAt = cellSlice.loadTlb(FutureSplitMerge)
        val r1 = cellSlice.loadRef(ShardDescrAux)
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

    override fun storeTlb(cellBuilder: CellBuilder, value: ShardDescrNew) {
        cellBuilder.storeUInt32(value.seqNo)
        cellBuilder.storeUInt32(value.regMcSeqno)
        cellBuilder.storeUInt64(value.startLt)
        cellBuilder.storeUInt64(value.endLt)
        cellBuilder.storeBits(value.rootHash)
        cellBuilder.storeBits(value.fileHash)
        cellBuilder.storeBit(value.beforeSplit)
        cellBuilder.storeBit(value.beforeMerge)
        cellBuilder.storeBit(value.wantSplit)
        cellBuilder.storeBit(value.wantMerge)
        cellBuilder.storeBit(value.nxCcUpdated)
        cellBuilder.storeInt(value.flags, 3)
        cellBuilder.storeUInt32(value.nextCatchainSeqno)
        cellBuilder.storeUInt64(value.nextValidatorShard)
        cellBuilder.storeUInt32(value.minRefMcSeqno)
        cellBuilder.storeUInt32(value.genUtime)
        cellBuilder.storeTlb(FutureSplitMerge, value.splitMergeAt)
        cellBuilder.storeRef(ShardDescrAux, value.r1)
    }
}

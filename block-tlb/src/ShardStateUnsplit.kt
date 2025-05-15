package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.hashmap.HashMapE
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


@SerialName("shard_state")
public data class ShardStateUnsplit(
    @SerialName("global_id") val globalId: Int, // global_id : int32
    @SerialName("shard_id") val shardId: ShardIdent, // shard_id : ShardIdent
    @SerialName("seq_no") val seqNo: UInt, // seq_no : uint32
    @SerialName("vert_seq_no") val vertSeqNo: Int, // vert_seq_no : #
    @SerialName("gen_utime") val genUtime: UInt, // gen_utime : uint32
    @SerialName("gen_lt") val genLt: ULong, // gen_lt : uint64
    @SerialName("min_ref_mc_seqno") val minRefMcSeqno: UInt, // min_ref_mc_seqno : uint32
    @SerialName("out_msg_queue_info") val outMsgQueueInfo: CellRef<OutMsgQueueInfo>, // out_msg_queue_info : ^OutMsgQueueInfo
    @SerialName("before_split") val beforeSplit: Boolean, // before_split : ## 1
    val accounts: CellRef<ShardAccounts>, // accounts : ^ShardAccounts
    val r1: CellRef<ShardStateUnsplitAux>, // ^[$_ overload_history:uint64 underload_history:uint64 total_balance:CurrencyCollection total_validator_fees:CurrencyCollection libraries:(HashmapE 256 LibDescr) master_ref:(Maybe BlkMasterInfo) ]
    val custom: Maybe<CellRef<McStateExtra>> // custom : Maybe ^McStateExtra
) : ShardState {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer.type("shard_state") {
            field("global_id", globalId)
            field("shard_id", shardId)
            field("seq_no", seqNo)
            field("vert_seq_no", vertSeqNo)
            field("gen_utime", genUtime)
            field("gen_lt", genLt)
            field("min_ref_mc_seqno", minRefMcSeqno)
            field("out_msg_queue_info", outMsgQueueInfo)
            field("before_split", beforeSplit)
            field("accounts", accounts)
            field(r1)
            field("custom", custom)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<ShardStateUnsplit> by ShardStateUnsplitTlbConstructor
}


public data class ShardStateUnsplitAux(
    @SerialName("overload_history") val overloadHistory: ULong, // overload_history : uint64
    @SerialName("underload_history") val underloadHistory: ULong, // underload_history : uint64
    @SerialName("total_balance") val totalBalance: CurrencyCollection, // total_balance : CurrencyCollection
    @SerialName("total_validator_fees") val totalValidatorFees: CurrencyCollection, // total_validator_fees : CurrencyCollection
    val libraries: HashMapE<LibDescr>, // libraries : HashmapE 256 LibDescr
    @SerialName("master_ref") val masterRef: Maybe<BlkMasterInfo>, // master_ref : Maybe BlkMasterInfo
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer.type {
            field("overload_history", overloadHistory)
            field("underload_history", underloadHistory)
            field("total_balance", totalBalance)
            field("total_validator_fees", totalValidatorFees)
            field("libraries", libraries)
            field("master_ref", masterRef)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<ShardStateUnsplitAux> by ShardStateUnsplitAuxTlbConstructor
}

private object ShardStateUnsplitTlbConstructor : TlbConstructor<ShardStateUnsplit>(
    schema = "shard_state#9023afe2 " +
            "global_id:int32 " +
            "shard_id:ShardIdent " +
            "seq_no:uint32 " +
            "vert_seq_no:# " +
            "gen_utime:uint32 " +
            "gen_lt:uint64 " +
            "min_ref_mc_seqno:uint32 " +
            "out_msg_queue_info:^OutMsgQueueInfo " +
            "before_split:(## 1) " +
            "accounts:^ShardAccounts " +
            "^[ overload_history:uint64 " +
            "underload_history:uint64 " +
            "total_balance:CurrencyCollection " +
            "total_validator_fees:CurrencyCollection " +
            "libraries:(HashmapE 256 LibDescr) " +
            "master_ref:(Maybe BlkMasterInfo) ] " +
            "custom:(Maybe ^McStateExtra) " +
            "= ShardStateUnsplit;"
) {
    private val maybeMcExtra = Maybe.tlbCodec(CellRef.tlbCodec(McStateExtra))

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: ShardStateUnsplit
    ) = cellBuilder {
        storeInt(value.globalId, 32)
        storeTlb(ShardIdent, value.shardId)
        storeUInt(value.seqNo.toInt(), 32)
        storeInt(value.vertSeqNo, 32)
        storeUInt(value.genUtime.toInt(), 32)
        storeUInt(value.genLt.toLong(), 64)
        storeUInt(value.minRefMcSeqno.toInt(), 32)
        storeRef(OutMsgQueueInfo, value.outMsgQueueInfo)
        storeBit(value.beforeSplit)
        storeRef(ShardAccounts, value.accounts)
        storeRef(ShardStateUnsplitAux, value.r1)
        storeTlb(maybeMcExtra, value.custom)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): ShardStateUnsplit = cellSlice {
        val globalId = loadInt(32).toInt()
        val shardId = loadTlb(ShardIdent)
        val seqNo = loadUInt(32).toInt().toUInt()
        val vertSeqNo = loadInt(32).toInt()
        val genUtime = loadUInt(32).toInt().toUInt()
        val genLt = loadUInt(64).toLong().toULong()
        val minRefMcSeqno = loadUInt(32).toInt().toUInt()
        val outMsgQueueInfo = loadRef(OutMsgQueueInfo)
        val beforeSplit = loadBit()
        val accounts = loadRef(ShardAccounts)
        val r1 = loadRef(ShardStateUnsplitAux)
        val custom = loadTlb(maybeMcExtra)
        ShardStateUnsplit(
            globalId,
            shardId,
            seqNo,
            vertSeqNo,
            genUtime,
            genLt,
            minRefMcSeqno,
            outMsgQueueInfo,
            beforeSplit,
            accounts,
            r1,
            custom
        )
    }
}

private object ShardStateUnsplitAuxTlbConstructor : TlbConstructor<ShardStateUnsplitAux>(
    schema = "[\$_ overload_history:uint64 " +
            "underload_history:uint64 " +
            "total_balance:CurrencyCollection " +
            "total_validator_fees:CurrencyCollection " +
            "libraries:(HashmapE 256 LibDescr) " +
            "master_ref:(Maybe BlkMasterInfo) ]"
) {
    private val hashMapELibDesc = HashMapE.tlbCodec(256, LibDescr)
    private val maybeBlkMasterInfo = Maybe.tlbCodec(BlkMasterInfo)

    override fun storeTlb(cellBuilder: CellBuilder, value: ShardStateUnsplitAux) {
        cellBuilder.storeUInt(value.overloadHistory.toLong(), 64)
        cellBuilder.storeUInt(value.underloadHistory.toLong(), 64)
        cellBuilder.storeTlb(CurrencyCollection, value.totalBalance)
        cellBuilder.storeTlb(CurrencyCollection, value.totalValidatorFees)
        cellBuilder.storeTlb(hashMapELibDesc, value.libraries)
        cellBuilder.storeTlb(maybeBlkMasterInfo, value.masterRef)
    }

    override fun loadTlb(cellSlice: CellSlice): ShardStateUnsplitAux {
        val overloadHistory = cellSlice.loadUInt(64).toLong().toULong()
        val underloadHistory = cellSlice.loadUInt(64).toLong().toULong()
        val totalBalance = cellSlice.loadTlb(CurrencyCollection)
        val totalValidatorFees = cellSlice.loadTlb(CurrencyCollection)
        val libraries = cellSlice.loadTlb(hashMapELibDesc)
        val masterRef = cellSlice.loadTlb(maybeBlkMasterInfo)
        return ShardStateUnsplitAux(
            overloadHistory = overloadHistory,
            underloadHistory = underloadHistory,
            totalBalance = totalBalance,
            totalValidatorFees = totalValidatorFees,
            libraries = libraries,
            masterRef = masterRef
        )
    }
}

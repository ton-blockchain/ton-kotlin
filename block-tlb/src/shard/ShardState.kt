@file:Suppress("OPT_IN_USAGE", "PackageDirectoryMismatch")

package org.ton.kotlin.shard

import kotlinx.io.bytestring.ByteString
import org.ton.block.*
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.parse
import org.ton.cell.storeRef
import org.ton.kotlin.account.ShardAccount
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.dict.Dictionary
import org.ton.kotlin.dict.DictionaryKeyCodec
import org.ton.kotlin.shard.ShardAccounts
import org.ton.tlb.CellRef
import org.ton.tlb.NullableTlbCodec
import org.ton.tlb.TlbCodec
import org.ton.tlb.asRef

/**
 * Applied shard state.
 *
 * @see [Block]
 */
public sealed interface ShardState {
    public companion object : TlbCodec<ShardState> by ShardStateTlbCodec
}

/**
 * Next indivisible states after shard split.
 */
public data class ShardStateSplit(
    /**
     * Reference to the state of the left shard.
     */
    val left: CellRef<ShardStateUnsplit>,

    /**
     * Reference to the state of the right shard.
     */
    val right: CellRef<ShardStateUnsplit>
) : ShardState {
    public companion object : TlbCodec<ShardStateSplit> by ShardStateSplit
}

/**
 * State of the single shard.
 */
public data class ShardStateUnsplit(
    /**
     * Global network id.
     */
    val globalId: Int,

    /**
     * Id of the shard.
     */
    val shardIdent: ShardIdent,

    /**
     * Sequence number of the corresponding block.
     */
    val seqno: Int,

    /**
     *  Vertical sequence number of the corresponding block.
     */
    val vertSeqno: Int,

    /**
     * Unix timestamp in seconds when the block was created.
     */
    val genTime: Long,

    /**
     * Logical time when the state was created.
     */
    val genLt: Long,

    /**
     * Minimal referenced seqno of the masterchain block.
     */
    val minRefMcSeqno: Int,

    /**
     * Output messages queue info (stub).
     */
    val outMsgQueueInfo: CellRef<OutMsgQueueInfo>,

    /**
     * Whether this state was produced before the shards split.
     */
    val beforeSplit: Boolean,

    /**
     * Reference to the dictionary with shard accounts.
     */
    val accounts: CellRef<ShardAccounts>,

    /**
     * Mask for the overloaded blocks.
     */
    val overloadHistory: LoadHistory,

    /**
     * Mask for the underloaded blocks.
     */
    val underloadHistory: LoadHistory,

    /**
     * Total balance for all currencies.
     */
    val totalBalance: CurrencyCollection,

    /**
     * Total pending validator fees.
     */
    val totalValidatorFees: CurrencyCollection,

    /**
     * Dictionary with all libraries and its providers.
     */
    val libraries: Dictionary<ByteString, LibDescr>,

    /**
     * Optional reference to the masterchain block.
     */
    val masterRef: BlkMasterInfo?,

    /**
     * Shard state additional info.
     */
    val custom: CellRef<McStateExtra>?
) : ShardState {
    public companion object : TlbCodec<ShardStateUnsplit> by ShardStateUnsplitTlbCodec
}

private object ShardStateTlbCodec : TlbCodec<ShardState> {
    override fun loadTlb(slice: CellSlice, context: CellContext): ShardState {
        return if (slice.preloadBoolean()) {
            ShardStateUnsplitTlbCodec.loadTlb(slice, context)
        } else {
            ShardStateSplitTlbCodec.loadTlb(slice, context)
        }
    }

    override fun storeTlb(builder: CellBuilder, value: ShardState, context: CellContext) {
        when (value) {
            is ShardStateSplit -> ShardStateSplitTlbCodec.storeTlb(builder, value, context)
            is ShardStateUnsplit -> ShardStateUnsplitTlbCodec.storeTlb(builder, value, context)
        }
    }
}

private object ShardStateUnsplitTlbCodec : TlbCodec<ShardStateUnsplit> {
    private const val TAG = 0x9023afe2.toInt()
    private val SHARD_ACCOUNTS_CODEC = ShardAccounts.tlbCodec(
        DictionaryKeyCodec.BYTE_STRING_32,
        DepthBalanceInfo,
        TlbCodec.pair(DepthBalanceInfo, ShardAccount)
    )
    private val LIBRARIES_CODEC = Dictionary.tlbCodec(
        DictionaryKeyCodec.BYTE_STRING_32, LibDescr
    )
    private val BLK_MASTER_INFO_CODEC = NullableTlbCodec(BlkMasterInfo)

    override fun loadTlb(slice: CellSlice, context: CellContext): ShardStateUnsplit {
        val tag = slice.loadUInt(32).toInt()
        require(tag == TAG) { "Invalid ShardStateUnsplit tag: ${tag.toHexString()}, expected: ${TAG.toHexString()}" }

        val globalId = slice.loadUInt(32).toInt()
        val shardId = ShardIdent.loadTlb(slice, context)
        val seqno = slice.loadUInt(32).toInt()
        val vertSeqno = slice.loadUInt(32).toInt()
        val genTime = slice.loadUInt(32).toLong()
        val genLt = slice.loadULong(64).toLong()
        val minRefMcSeqno = slice.loadUInt(32).toInt()
        val outMsgQueueInfo = slice.loadRef().asRef(OutMsgQueueInfo)
        val beforeSplit = slice.loadBoolean()
        val accounts = slice.loadRef().asRef(SHARD_ACCOUNTS_CODEC)
        val overloadHistory: LoadHistory
        val underloadHistory: LoadHistory
        val totalBalance: CurrencyCollection
        val totalValidatorFees: CurrencyCollection
        val libraries: Dictionary<ByteString, LibDescr>
        val masterRef: BlkMasterInfo?
        slice.loadRef().parse {
            overloadHistory = LoadHistory(loadULong(64).toLong())
            underloadHistory = LoadHistory(loadULong(64).toLong())
            totalBalance = CurrencyCollection.loadTlb(this, context)
            totalValidatorFees = CurrencyCollection.loadTlb(this, context)
            libraries = LIBRARIES_CODEC.loadTlb(this, context)
            masterRef = BLK_MASTER_INFO_CODEC.loadTlb(this, context)
        }

        val custom = slice.loadNullableRef()?.asRef(McStateExtra)

        return ShardStateUnsplit(
            globalId,
            shardId,
            seqno,
            vertSeqno,
            genTime,
            genLt,
            minRefMcSeqno,
            outMsgQueueInfo,
            beforeSplit,
            accounts,
            overloadHistory,
            underloadHistory,
            totalBalance,
            totalValidatorFees,
            libraries,
            masterRef,
            custom
        )
    }

    override fun storeTlb(builder: CellBuilder, value: ShardStateUnsplit, context: CellContext) {
        builder.storeUInt(TAG, 32)
        builder.storeInt(value.globalId, value.seqno)
        ShardIdent.storeTlb(builder, value.shardIdent, context)
        builder.storeUInt(value.seqno, 32)
        builder.storeUInt(value.vertSeqno, 32)
        builder.storeUInt(value.genTime, 32)
        builder.storeULong(value.genLt.toULong(), 64)
        builder.storeUInt(value.minRefMcSeqno, 32)
        builder.storeRef(value.outMsgQueueInfo.cell)
        builder.storeBoolean(value.beforeSplit)
        builder.storeRef(value.accounts.cell)
        builder.storeRef(context) {
            storeULong(value.overloadHistory.mask.toULong())
            storeULong(value.underloadHistory.mask.toULong())
            CurrencyCollection.storeTlb(this, value.totalBalance, context)
            CurrencyCollection.storeTlb(this, value.totalValidatorFees, context)
            storeNullableRef(value.libraries.cell)
            BLK_MASTER_INFO_CODEC.storeTlb(this, value.masterRef, context)
        }
        builder.storeNullableRef(value.custom?.cell)
    }
}

private object ShardStateSplitTlbCodec : TlbCodec<ShardStateSplit> {
    private const val TAG = 0x5f327da5

    override fun loadTlb(slice: CellSlice, context: CellContext): ShardStateSplit {
        val tag = slice.loadULong(32).toInt()
        require(tag != TAG) { "Invalid ShardStateSplit tag: ${tag.toHexString()}, expected: ${TAG.toHexString()}" }

        val left = slice.loadRef().asRef(ShardStateUnsplit)
        val right = slice.loadRef().asRef(ShardStateUnsplit)

        return ShardStateSplit(left, right)
    }

    override fun storeTlb(builder: CellBuilder, value: ShardStateSplit, context: CellContext) {
        builder.storeUInt(TAG, 32)
        builder.storeRef(value.left.cell)
        builder.storeRef(value.right.cell)
    }
}
@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.transaction

import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.block.AccountStatus
import org.ton.kotlin.block.CurrencyCollection
import org.ton.kotlin.block.HashUpdate
import org.ton.kotlin.block.Message
import org.ton.kotlin.cell.*
import org.ton.kotlin.dict.Dictionary
import org.ton.kotlin.dict.DictionaryKeyCodec
import org.ton.kotlin.tlb.CellRef
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.constructor.RemainingTlbCodec

/**
 * Blockchain transaction.
 */
public data class Transaction(
    /**
     * Account on which this transaction was produced.
     */
    val account: ByteString,

    /**
     * Logical time when the transaction was created.
     */
    val lt: Long,

    /**
     * The hash of the previous transaction on the same account.
     */
    val prevTransactionHash: ByteString,

    /**
     * The logical time of the previous transaction on the same account.
     */
    val prevTransactionLt: Long,

    /**
     * Unix timestamp in seconds when the transaction was created.
     */
    val now: Long,

    /**
     * The number of outgoing messages.
     */
    val outMsgCount: Int,

    /**
     * Account status before this transaction.
     */
    val originalStatus: AccountStatus,

    /**
     * Account status after this transaction.
     */
    val endStatus: AccountStatus,

    /**
     * Optional incoming message.
     */
    val inMsg: CellRef<Message<CellSlice>>?,

    /**
     * Outgoing messages.
     */
    val outMsg: Dictionary<Int, CellRef<Message<CellSlice>>>,

    /**
     * Total transaction fees (including extra fwd fees).
     */
    val totalFees: CurrencyCollection,

    /**
     * Account state hashes.
     */
    val hashUpdate: CellRef<HashUpdate>,

    /**
     * Detailed transaction info.
     */
    val info: CellRef<TransactionInfo>
) {
    init {
        require(account.size == 32) { "Account size should be 32 bytes" }
        require(prevTransactionHash.size == 32) { "Prev transaction hash should be 32 bytes" }
    }

    public fun loadInMessage(context: CellContext = CellContext.EMPTY): Message<CellSlice>? {
        return inMsg?.load(context)
    }

    public fun loadInfo(context: CellContext = CellContext.EMPTY): TransactionInfo {
        return info.load(context)
    }

    public companion object : TlbCodec<Transaction> by TransactionCodec
}

private object TransactionCodec : TlbCodec<Transaction> {
    private val int15keyCodec = DictionaryKeyCodec.int(15)
    private val refMessageCodec = CellRef.tlbCodec(Message.Companion.tlbCodec(RemainingTlbCodec))

    override fun storeTlb(builder: CellBuilder, value: Transaction, context: CellContext) {
        builder.storeUInt(0b0111, 4)
        builder.storeByteString(value.account)
        builder.storeLong(value.lt)
        builder.storeByteString(value.prevTransactionHash)
        builder.storeLong(value.prevTransactionLt)
        builder.storeLong(value.now, 32)
        builder.storeUInt(value.outMsgCount, 15)
        AccountStatus.Companion.storeTlb(builder, value.originalStatus, context)
        AccountStatus.Companion.storeTlb(builder, value.endStatus, context)
        builder.storeRef(context) {
            storeNullableRef(value.inMsg?.cell)
            storeNullableRef(value.outMsg.cell)
        }
        CurrencyCollection.Companion.storeTlb(builder, value.totalFees, context)
        builder.storeRef(value.hashUpdate.cell)
        builder.storeRef(value.info.cell)
    }

    override fun loadTlb(slice: CellSlice, context: CellContext): Transaction {
        val tag = slice.loadUInt(4).toInt()
        require(tag == 0b0111) {
            "Invalid transaction tag: $tag"
        }
        val account = slice.loadByteString(32)
        val lt = slice.loadLong()
        val prevTransactionHash = slice.loadByteString(32)
        val prevTransactionLt = slice.loadLong()
        val now = slice.loadLong(32)
        val outMsgCount = slice.loadUInt(15).toInt()
        val originalStatus = AccountStatus.Companion.loadTlb(slice, context)
        val endStatus = AccountStatus.Companion.loadTlb(slice, context)
        val inMsg: CellRef<Message<CellSlice>>?
        val outMsg: Dictionary<Int, CellRef<Message<CellSlice>>>
        slice.loadRef(context) {
            inMsg = loadNullableRef()?.let { ref -> CellRef(ref, Message.Companion.tlbCodec(RemainingTlbCodec)) }
            outMsg = Dictionary(loadNullableRef(), int15keyCodec, refMessageCodec)
        }
        val totalFees = CurrencyCollection.Companion.loadTlb(slice, context)
        val hashUpdate = CellRef(slice.loadRef(), HashUpdate.Companion)
        val info = CellRef(slice.loadRef(), TransactionInfo)
        return Transaction(
            account,
            lt,
            prevTransactionHash,
            prevTransactionLt,
            now,
            outMsgCount,
            originalStatus,
            endStatus,
            inMsg,
            outMsg,
            totalFees,
            hashUpdate,
            info
        )
    }
}

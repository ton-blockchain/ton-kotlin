package org.ton.sdk.blockchain.transaction

import org.ton.cell.CellSlice
import org.ton.kotlin.dict.Dictionary
import org.ton.sdk.blockchain.account.AccountStatus
import org.ton.sdk.blockchain.currency.CurrencyCollection
import org.ton.sdk.blockchain.message.Message
import org.ton.sdk.crypto.HashBytes
import org.ton.tlb.CellRef
import kotlin.jvm.JvmName

/**
 * Blockchain transaction.
 */
public class Transaction(
    /**
     * Account on which this transaction was produced.
     */
    @get:JvmName("account")
    public val account: HashBytes,

    /**
     * Logical time when the transaction was created.
     */
    @get:JvmName("lt")
    public val lt: Long,

    /**
     * The hash of the previous transaction on the same account.
     */
    @get:JvmName("prevTransactionHash")
    public val prevTransactionHash: HashBytes,

    /**
     * The logical time of the previous transaction on the same account.
     */
    @get:JvmName("prevTransactionLt")
    public val prevTransactionLt: Long,

    /**
     * Unix timestamp in seconds when the transaction was created.
     */
    @get:JvmName("now")
    public val now: Long,

    /**
     * The number of outgoing messages.
     */
    @get:JvmName("outMsgCount")
    public val outMsgCount: Int,

    /**
     * Account status before this transaction.
     */
    @get:JvmName("originalStatus")
    public val originalStatus: AccountStatus,

    /**
     * Account status after this transaction.
     */
    @get:JvmName("endStatus")
    public val endStatus: AccountStatus,

    /**
     * Optional incoming message.
     */
    @get:JvmName("inMsg")
    public val inMsg: CellRef<Message<CellSlice>>?,

    /**
     * Outgoing messages.
     */
    @get:JvmName("outMsgs")
    public val outMsgs: Dictionary<Int, CellRef<Message<CellSlice>>>,

    /**
     * Total transaction fees (including extra fwd fees).
     */
    @get:JvmName("totalFees")
    public val totalFees: CurrencyCollection,

    /**
     * Account state hashes.
     */
    @get:JvmName("hashUpdate")
    public val hashUpdate: CellRef<HashUpdate>,

    /**
     * Detailed transaction info.
     */
    @get:JvmName("description")
    public val description: CellRef<TransactionDescription>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Transaction

        if (lt != other.lt) return false
        if (prevTransactionLt != other.prevTransactionLt) return false
        if (now != other.now) return false
        if (outMsgCount != other.outMsgCount) return false
        if (account != other.account) return false
        if (prevTransactionHash != other.prevTransactionHash) return false
        if (originalStatus != other.originalStatus) return false
        if (endStatus != other.endStatus) return false
        if (inMsg != other.inMsg) return false
        if (outMsgs != other.outMsgs) return false
        if (totalFees != other.totalFees) return false
        if (hashUpdate != other.hashUpdate) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lt.hashCode()
        result = 31 * result + prevTransactionLt.hashCode()
        result = 31 * result + now.hashCode()
        result = 31 * result + outMsgCount
        result = 31 * result + account.hashCode()
        result = 31 * result + prevTransactionHash.hashCode()
        result = 31 * result + originalStatus.hashCode()
        result = 31 * result + endStatus.hashCode()
        result = 31 * result + (inMsg?.hashCode() ?: 0)
        result = 31 * result + outMsgs.hashCode()
        result = 31 * result + totalFees.hashCode()
        result = 31 * result + hashUpdate.hashCode()
        result = 31 * result + description.hashCode()
        return result
    }

    override fun toString(): String = buildString {
        append("Transaction(account=")
        append(account)
        append(", lt=")
        append(lt)
        append(", prevTransactionHash=")
        append(prevTransactionHash)
        append(", prevTransactionLt=")
        append(prevTransactionLt)
        append(", now=")
        append(now)
        append(", outMsgCount=")
        append(outMsgCount)
        append(", originalStatus=")
        append(originalStatus)
        append(", endStatus=")
        append(endStatus)
        append(", inMsg=")
        append(inMsg)
        append(", outMsgs=")
        append(outMsgs)
        append(", totalFees=")
        append(totalFees)
        append(", hashUpdate=")
        append(hashUpdate)
        append(", description=")
        append(description)
        append(")")
    }
}

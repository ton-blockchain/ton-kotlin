package org.ton.sdk.blockchain.transaction

import org.ton.cell.CellSlice
import org.ton.sdk.blockchain.account.AccountStatus
import org.ton.sdk.blockchain.currency.CurrencyCollection
import org.ton.sdk.blockchain.message.Message
import org.ton.sdk.cell.CellRef
import org.ton.sdk.crypto.HashBytes
import org.ton.sdk.dict.Dictionary

/**
 * Blockchain transaction.
 */
public class Transaction(
    /**
     * Account on which this transaction was produced.
     */
    public val account: HashBytes,

    /**
     * Logical time when the transaction was created.
     */
    public val lt: Long,

    /**
     * The hash of the previous transaction on the same account.
     */
    public val prevTransactionHash: HashBytes,

    /**
     * The logical time of the previous transaction on the same account.
     */
    public val prevTransactionLt: Long,

    /**
     * Unix timestamp in seconds when the transaction was created.
     */
    public val now: Long,

    /**
     * The number of outgoing messages.
     */
    public val outMsgCount: Int,

    /**
     * Account status before this transaction.
     */
    public val originalStatus: AccountStatus,

    /**
     * Account status after this transaction.
     */
    public val endStatus: AccountStatus,

    /**
     * Optional incoming message.
     */
    public val inMsg: CellRef<Message<CellSlice>>?,

    /**
     * Outgoing messages.
     */
    public val outMsgs: Dictionary<Int, CellRef<Message<CellSlice>>>,

    /**
     * Total transaction fees (including extra fwd fees).
     */
    public val totalFees: CurrencyCollection,

    /**
     * Account state hashes.
     */
    public val hashUpdate: CellRef<HashUpdate>,

    /**
     * Detailed transaction info.
     */
    public val description: CellRef<TransactionDescription>
)

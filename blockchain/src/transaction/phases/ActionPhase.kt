package org.ton.sdk.blockchain.transaction.phases

import org.ton.kotlin.crypto.HashBytes
import org.ton.sdk.blockchain.account.StorageUsedShort
import org.ton.sdk.blockchain.currency.Coins

/**
 * Action phase info.
 *
 * At this phase, the list of actions from the compute phase
 * is converted into updates and outgoing messages.
 *
 * @see [org.ton.sdk.blockchain.transaction.TransactionDescription]
 */
public class ActionPhase(
    /**
     * Whether the execution was successful.
     */
    public val isSuccess: Boolean,

    /**
     *  Whether the action list was valid.
     */
    public val isValid: Boolean,

    /**
     * There were no funds to create an outgoing message.
     */
    public val noFunds: Boolean,

    /**
     * Account status change during execution of this phase.
     */
    public val statusChange: AccountStatusChange,

    /**
     * Total forwarding fee for outgoing messages.
     */
    public val totalFwdFees: Coins?,

    /**
     * Total fees for processing all actions.
     */
    public val totalActionFees: Coins?,

    /**
     * Result code of the phase.
     */
    public val resultCode: Int,

    /**
     * Optional result argument of the phase.
     */
    public val resultArg: Int?,

    /**
     * The total number of processed actions.
     */
    public val totalActions: Int,

    /**
     * The number of special actions (`ReserveCurrency`, `SetCode`, `ChangeLibrary`, copyleft).
     */
    public val specialActions: Int,

    /**
     * The number of skipped actions.
     */
    public val skippedActions: Int,

    /**
     * The number of outgoing messages created by the compute phase.
     */
    public val messagesCreated: Int,

    /**
     * The hash of the actions list.
     */
    public val actionListHash: HashBytes,

    /**
     * The total number of unique cells (bits / refs) of produced messages.
     */
    public val totalMessageSize: StorageUsedShort
) {
    init {
        require(totalActions in 0..UShort.MAX_VALUE.toInt()) { "Expected totalActions in range [0..65535], but was $totalActions" }
        require(specialActions in 0..UShort.MAX_VALUE.toInt()) { "Expected specialActions in range [0..65535], but was $specialActions" }
        require(skippedActions in 0..UShort.MAX_VALUE.toInt()) { "Expected skippedActions in range [0..65535], but was $skippedActions" }
        require(messagesCreated in 0..UShort.MAX_VALUE.toInt()) { "Expected messagesCreated in range [0..65535], but was $messagesCreated" }
    }
}

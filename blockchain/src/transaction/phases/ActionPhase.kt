package org.ton.sdk.blockchain.transaction.phases

import org.ton.sdk.blockchain.account.StorageUsedShort
import org.ton.sdk.blockchain.currency.Coins
import org.ton.sdk.crypto.HashBytes
import kotlin.jvm.JvmName

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
    @get:JvmName("noFunds")
    public val noFunds: Boolean,

    /**
     * Account status change during execution of this phase.
     */
    @get:JvmName("statusChange")
    public val statusChange: AccountStatusChange,

    /**
     * Total forwarding fee for outgoing messages.
     */
    @get:JvmName("totalFwdFees")
    public val totalFwdFees: Coins?,

    /**
     * Total fees for processing all actions.
     */
    @get:JvmName("totalActionFees")
    public val totalActionFees: Coins?,

    /**
     * Result code of the phase.
     */
    @get:JvmName("resultCode")
    public val resultCode: Int,

    /**
     * Optional result argument of the phase.
     */
    @get:JvmName("resultArg")
    public val resultArg: Int?,

    /**
     * The total number of processed actions.
     */
    @get:JvmName("totalActions")
    public val totalActions: Int,

    /**
     * The number of special actions (`ReserveCurrency`, `SetCode`, `ChangeLibrary`, copyleft).
     */
    @get:JvmName("specialActions")
    public val specialActions: Int,

    /**
     * The number of skipped actions.
     */
    @get:JvmName("skippedActions")
    public val skippedActions: Int,

    /**
     * The number of outgoing messages created by the compute phase.
     */
    @get:JvmName("messagesCreated")
    public val messagesCreated: Int,

    /**
     * The hash of the actions list.
     */
    @get:JvmName("actionListHash")
    public val actionListHash: HashBytes,

    /**
     * The total number of unique cells (bits / refs) of produced messages.
     */
    @get:JvmName("totalMessageSize")
    public val totalMessageSize: StorageUsedShort
) {
    init {
        require(totalActions in 0..UShort.MAX_VALUE.toInt()) { "Expected totalActions in range [0..65535], but was $totalActions" }
        require(specialActions in 0..UShort.MAX_VALUE.toInt()) { "Expected specialActions in range [0..65535], but was $specialActions" }
        require(skippedActions in 0..UShort.MAX_VALUE.toInt()) { "Expected skippedActions in range [0..65535], but was $skippedActions" }
        require(messagesCreated in 0..UShort.MAX_VALUE.toInt()) { "Expected messagesCreated in range [0..65535], but was $messagesCreated" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ActionPhase

        if (isSuccess != other.isSuccess) return false
        if (isValid != other.isValid) return false
        if (noFunds != other.noFunds) return false
        if (resultCode != other.resultCode) return false
        if (resultArg != other.resultArg) return false
        if (totalActions != other.totalActions) return false
        if (specialActions != other.specialActions) return false
        if (skippedActions != other.skippedActions) return false
        if (messagesCreated != other.messagesCreated) return false
        if (statusChange != other.statusChange) return false
        if (totalFwdFees != other.totalFwdFees) return false
        if (totalActionFees != other.totalActionFees) return false
        if (actionListHash != other.actionListHash) return false
        if (totalMessageSize != other.totalMessageSize) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isSuccess.hashCode()
        result = 31 * result + isValid.hashCode()
        result = 31 * result + noFunds.hashCode()
        result = 31 * result + resultCode
        result = 31 * result + (resultArg ?: 0)
        result = 31 * result + totalActions
        result = 31 * result + specialActions
        result = 31 * result + skippedActions
        result = 31 * result + messagesCreated
        result = 31 * result + statusChange.hashCode()
        result = 31 * result + (totalFwdFees?.hashCode() ?: 0)
        result = 31 * result + (totalActionFees?.hashCode() ?: 0)
        result = 31 * result + actionListHash.hashCode()
        result = 31 * result + totalMessageSize.hashCode()
        return result
    }

    override fun toString(): String = buildString {
        append("ActionPhase(isSuccess=")
        append(isSuccess)
        append(", isValid=")
        append(isValid)
        append(", noFunds=")
        append(noFunds)
        append(", statusChange=")
        append(statusChange)
        append(", totalFwdFees=")
        append(totalFwdFees)
        append(", totalActionFees=")
        append(totalActionFees)
        append(", resultCode=")
        append(resultCode)
        append(", resultArg=")
        append(resultArg)
        append(", totalActions=")
        append(totalActions)
        append(", specialActions=")
        append(specialActions)
        append(", skippedActions=")
        append(skippedActions)
        append(", messagesCreated=")
        append(messagesCreated)
        append(", actionListHash=")
        append(actionListHash)
        append(", totalMessageSize=")
        append(totalMessageSize)
        append(")")
    }
}

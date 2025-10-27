package org.ton.sdk.blockchain.transaction.phases

import org.ton.sdk.blockchain.currency.Coins
import org.ton.sdk.blockchain.currency.CurrencyCollection
import kotlin.jvm.JvmName

/**
 * Credit phase info.
 *
 * At this phase message balance is added to the account balance.
 *
 * @see [org.ton.sdk.blockchain.transaction.TransactionDescription]
 */
public class CreditPhase(
    /**
     * Number of coins paid for the debt.
     */
    @get:JvmName("dueFeesCollected")
    public val dueFeesCollected: Coins?,

    /**
     * Number of tokens added to the account balance from the remaining message balance.
     */
    @get:JvmName("credit")
    public val credit: CurrencyCollection
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CreditPhase

        if (dueFeesCollected != other.dueFeesCollected) return false
        if (credit != other.credit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dueFeesCollected?.hashCode() ?: 0
        result = 31 * result + credit.hashCode()
        return result
    }

    override fun toString(): String = "CreditPhase(dueFeesCollected=$dueFeesCollected, credit=$credit)"
}

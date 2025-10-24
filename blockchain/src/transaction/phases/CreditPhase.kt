package org.ton.sdk.blockchain.transaction.phases

import org.ton.sdk.blockchain.currency.Coins
import org.ton.sdk.blockchain.currency.CurrencyCollection

/**
 * Credit phase info.
 *
 * At this phase message balance is added to the account balance.
 *
 * @see [org.ton.sdk.blockchain.transaction.TransactionDescription]
 */
public class CreditPhase(
    /**
     * Amount of coins paid for the debt.
     */
    public val dueFeesCollected: Coins?,

    /**
     * Amount of tokens added to the account balance from the remaining message balance.
     */
    public val credit: CurrencyCollection
)

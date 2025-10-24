package org.ton.sdk.blockchain.transaction.phases

import org.ton.sdk.blockchain.currency.Coins

/**
 * Storage phase info.
 *
 * At this phase, the account pays for storing its state.
 *
 * @see [org.ton.sdk.blockchain.transaction.TransactionDescription]
 */
public class StoragePhase(
    /**
     * Amount of coins collected for storing this contract for some time.
     */
    public val storageFeesCollected: Coins,

    /**
     * Amount of coins which this account owes to the network
     * (if there was not enough balance to pay storage fee).
     */
    public val storageFeesDue: Coins?,

    /**
     *  Account status change during execution of this phase.
     */
    public val statusChange: AccountStatusChange
)

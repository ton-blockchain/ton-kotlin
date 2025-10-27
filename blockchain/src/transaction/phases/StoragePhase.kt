package org.ton.sdk.blockchain.transaction.phases

import org.ton.sdk.blockchain.currency.Coins
import kotlin.jvm.JvmName

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
    @get:JvmName("storageFeesCollected")
    public val storageFeesCollected: Coins,

    /**
     * Amount of coins which this account owes to the network
     * (if there was not enough balance to pay storage fee).
     */
    @get:JvmName("storageFeesDue")
    public val storageFeesDue: Coins?,

    /**
     *  Account status change during execution of this phase.
     */
    @get:JvmName("statusChange")
    public val statusChange: AccountStatusChange
)

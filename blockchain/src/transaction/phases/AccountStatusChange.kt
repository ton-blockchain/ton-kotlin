package org.ton.sdk.blockchain.transaction.phases

/**
 * Account status change during transaction execution.
 */
public enum class AccountStatusChange {
    /**
     * Account status has not changed.
     */
    UNCHANGED,

    /**
     * Account has been frozen.
     */
    FROZEN,

    /**
     * Account deleted.
     */
    DELETED,
}

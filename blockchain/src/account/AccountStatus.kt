package org.ton.sdk.blockchain.account

/**
 * Brief account status.
 */
public enum class AccountStatus {
    /**
     * Account exists but has not yet been deployed.
     */
    UNINIT,

    /**
     * Account exists but has been frozen.
     */
    FROZEN,

    /**
     * Account exists and has been deployed.
     */
    ACTIVE,

    /**
     * Account does not exist.
     */
    NONEXIST
}

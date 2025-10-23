package org.ton.kotlin.blockchain.account

import kotlinx.serialization.Serializable

/**
 * Brief account status.
 */
@Serializable
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
    NOT_EXISTS
}

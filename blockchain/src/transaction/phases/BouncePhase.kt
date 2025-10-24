package org.ton.sdk.blockchain.transaction.phases

import org.ton.sdk.blockchain.account.StorageUsedShort
import org.ton.sdk.blockchain.currency.Coins

/**
 * Bounce phase info.
 *
 * At this phase, some funds are returned to the sender.
 *
 * @see [org.ton.sdk.blockchain.transaction.TransactionDescription]
 */
public sealed interface BouncePhase {
    /**
     * Skipped bounce phase info.
     */
    public class NoFunds(
        /**
         * The total number of unique cells (bits / refs) of the bounced message.
         */
        public val messageSize: StorageUsedShort,

        /**
         * Required amount of coins to send the bounced message.
         */
        public val requiredForwardFees: Coins
    ) : BouncePhase

    /**
     * Bounce phase was executed.
     */
    public class Executed(
        /**
         * The total number of unique cells (bits / refs) of the bounced message.
         */
        public val messageSize: StorageUsedShort,

        /**
         * The part of fees for the validators.
         */
        public val messageFees: Coins,

        /**
         * Message forwarding fee.
         */
        public val forwardFees: Coins,
    ) : BouncePhase
}

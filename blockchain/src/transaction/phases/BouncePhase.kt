package org.ton.sdk.blockchain.transaction.phases

import org.ton.sdk.blockchain.account.StorageUsedShort
import org.ton.sdk.blockchain.currency.Coins
import kotlin.jvm.JvmName

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
        @get:JvmName("messageSize")
        public val messageSize: StorageUsedShort,

        /**
         * Required amount of coins to send the bounced message.
         */
        @get:JvmName("requiredForwardFees")
        public val requiredForwardFees: Coins
    ) : BouncePhase {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as NoFunds

            if (messageSize != other.messageSize) return false
            if (requiredForwardFees != other.requiredForwardFees) return false

            return true
        }

        override fun hashCode(): Int {
            var result = messageSize.hashCode()
            result = 31 * result + requiredForwardFees.hashCode()
            return result
        }

        override fun toString(): String = "NoFunds(messageSize=$messageSize, requiredForwardFees=$requiredForwardFees)"
    }

    /**
     * Bounce phase was executed.
     */
    public class Executed(
        /**
         * The total number of unique cells (bits / refs) of the bounced message.
         */
        @get:JvmName("messageSize")
        public val messageSize: StorageUsedShort,

        /**
         * The part of fees for the validators.
         */
        @get:JvmName("messageFees")
        public val messageFees: Coins,

        /**
         * Message forwarding fee.
         */
        @get:JvmName("forwardFees")
        public val forwardFees: Coins,
    ) : BouncePhase {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Executed

            if (messageSize != other.messageSize) return false
            if (messageFees != other.messageFees) return false
            if (forwardFees != other.forwardFees) return false

            return true
        }

        override fun hashCode(): Int {
            var result = messageSize.hashCode()
            result = 31 * result + messageFees.hashCode()
            result = 31 * result + forwardFees.hashCode()
            return result
        }

        override fun toString(): String =
            "Executed(messageSize=$messageSize, messageFees=$messageFees, forwardFees=$forwardFees)"
    }
}

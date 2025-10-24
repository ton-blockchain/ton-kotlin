package org.ton.sdk.blockchain.transaction.phases

import org.ton.kotlin.crypto.HashBytes
import org.ton.sdk.blockchain.currency.Coins

private const val UINT56_MAX_VALUE = (1L shl (7 * Byte.SIZE_BITS)) - 1
private const val UINT24_MAX_VALUE = (1 shl (3 * Byte.SIZE_BITS)) - 1

/**
 * Compute phase info.
 *
 * At this phase the VM is executed to produce a list of actions.
 *
 * @see [org.ton.sdk.blockchain.transaction.TransactionDescription]
 */
public sealed interface ComputePhase {
    public val skipReason: Skipped?

    /**
     * Skipped compute phase info.
     */
    public enum class Skipped : ComputePhase {
        /**
         * Contract doesn't have state to execute.
         */
        NO_STATE,

        /**
         * Contract state is invalid.
         */
        BAD_STATE,

        /**
         * Not enough gas to execute compute phase.
         */
        NO_GAS,

        /**
         * Account was suspended by the config.
         */
        SUSPENDED;

        override val skipReason: Skipped get() = this
    }

    /**
     * Executed compute phase info.
     */
    public class Executed(
        /**
         * Whether the execution was successful.
         */
        public val isSuccess: Boolean,

        /**
         * Whether the `init` from the incoming message was used.
         */
        public val isMsgStateUsed: Boolean,

        /**
         * Whether the account state changed to `Active` during this phase.
         */
        public val isAccountActivated: Boolean,

        /**
         * Total amount of tokens spent to execute this phase.
         */
        public val gasFees: Coins,

        /**
         * Amount of gas used by the VM to execute this phase.
         */
        public val gasUsed: Long,

        /**
         * Max gas amount which could be used.
         */
        public val gasLimit: Long,

        /**
         * Max gas amount which could be used before accepting this transaction.
         */
        public val gasCredit: Int?,

        /**
         * Execution mode.
         */
        public val mode: Byte,

        /**
         * VM exit code.
         */
        public val exitCode: Int,

        /**
         * Additional VM exit argument.
         */
        public val exitArg: Int?,

        /**
         * The number of VM steps it took to complete this phase.
         */
        public val vmSteps: Int,

        /**
         * Hash of the initial state of the VM.
         */
        public val vmInitStateHash: HashBytes,

        /**
         * Hash of the VM state after executing this phase.
         */
        public val vmFinalStateHash: HashBytes,
    ) : ComputePhase {
        override val skipReason: Skipped? get() = null

        init {
            require(gasUsed in 0..UINT56_MAX_VALUE) { "Expected gasUsed in range [0..$UINT56_MAX_VALUE], but was $gasUsed" }
            require(gasLimit in 0..UINT56_MAX_VALUE) { "Expected gasLimit in range [0..$UINT56_MAX_VALUE], but was $gasLimit" }
            require(gasCredit == null || gasCredit in 0..UINT24_MAX_VALUE) { "Expected gasCredit in range [0..$UINT24_MAX_VALUE], but was $gasCredit" }
        }
    }
}

@file:Suppress("INAPPLICABLE_JVM_NAME") // https://youtrack.jetbrains.com/issue/KT-31420

package org.ton.sdk.blockchain.transaction.phases

import org.ton.sdk.blockchain.currency.Coins
import org.ton.sdk.crypto.HashBytes
import kotlin.jvm.JvmName

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
    /**
     * Skip reason if the phase was skipped.
     */
    @get:JvmName("skipReason")
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

        @get:JvmName("skipReason")
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
        @get:JvmName("gasFees")
        public val gasFees: Coins,

        /**
         * Amount of gas used by the VM to execute this phase.
         */
        @get:JvmName("gasUsed")
        public val gasUsed: Long,

        /**
         * Max gas amount which could be used.
         */
        @get:JvmName("gasLimit")
        public val gasLimit: Long,

        /**
         * Max gas amount which could be used before accepting this transaction.
         */
        @get:JvmName("gasCredit")
        public val gasCredit: Int?,

        /**
         * Execution mode.
         */
        @get:JvmName("mode")
        public val mode: Byte,

        /**
         * VM exit code.
         */
        @get:JvmName("exitCode")
        public val exitCode: Int,

        /**
         * Additional VM exit argument.
         */
        @get:JvmName("exitArg")
        public val exitArg: Int?,

        /**
         * The number of VM steps it took to complete this phase.
         */
        @get:JvmName("vmSteps")
        public val vmSteps: Int,

        /**
         * Hash of the initial state of the VM.
         */
        @get:JvmName("vmInitStateHash")
        public val vmInitStateHash: HashBytes,

        /**
         * Hash of the VM state after executing this phase.
         */
        @get:JvmName("vmFinalStateHash")
        public val vmFinalStateHash: HashBytes,
    ) : ComputePhase {
        @get:JvmName("skipReason")
        override val skipReason: Skipped? get() = null

        init {
            require(gasUsed in 0..UINT56_MAX_VALUE) { "Expected gasUsed in range [0..$UINT56_MAX_VALUE], but was $gasUsed" }
            require(gasLimit in 0..UINT56_MAX_VALUE) { "Expected gasLimit in range [0..$UINT56_MAX_VALUE], but was $gasLimit" }
            require(gasCredit == null || gasCredit in 0..UINT24_MAX_VALUE) { "Expected gasCredit in range [0..$UINT24_MAX_VALUE], but was $gasCredit" }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Executed

            if (isSuccess != other.isSuccess) return false
            if (isMsgStateUsed != other.isMsgStateUsed) return false
            if (isAccountActivated != other.isAccountActivated) return false
            if (gasUsed != other.gasUsed) return false
            if (gasLimit != other.gasLimit) return false
            if (gasCredit != other.gasCredit) return false
            if (mode != other.mode) return false
            if (exitCode != other.exitCode) return false
            if (exitArg != other.exitArg) return false
            if (vmSteps != other.vmSteps) return false
            if (gasFees != other.gasFees) return false
            if (vmInitStateHash != other.vmInitStateHash) return false
            if (vmFinalStateHash != other.vmFinalStateHash) return false

            return true
        }

        override fun hashCode(): Int {
            var result = isSuccess.hashCode()
            result = 31 * result + isMsgStateUsed.hashCode()
            result = 31 * result + isAccountActivated.hashCode()
            result = 31 * result + gasUsed.hashCode()
            result = 31 * result + gasLimit.hashCode()
            result = 31 * result + (gasCredit ?: 0)
            result = 31 * result + mode
            result = 31 * result + exitCode
            result = 31 * result + (exitArg ?: 0)
            result = 31 * result + vmSteps
            result = 31 * result + gasFees.hashCode()
            result = 31 * result + vmInitStateHash.hashCode()
            result = 31 * result + vmFinalStateHash.hashCode()
            return result
        }

        override fun toString(): String = buildString {
            append("Executed(isSuccess=")
            append(isSuccess)
            append(", isMsgStateUsed=")
            append(isMsgStateUsed)
            append(", isAccountActivated=")
            append(isAccountActivated)
            append(", gasFees=")
            append(gasFees)
            append(", gasUsed=")
            append(gasUsed)
            append(", gasLimit=")
            append(gasLimit)
            append(", gasCredit=")
            append(gasCredit)
            append(", mode=")
            append(mode)
            append(", exitCode=")
            append(exitCode)
            append(", exitArg=")
            append(exitArg)
            append(", vmSteps=")
            append(vmSteps)
            append(", vmInitStateHash=")
            append(vmInitStateHash)
            append(", vmFinalStateHash=")
            append(vmFinalStateHash)
            append(", skipReason=")
            append(skipReason)
            append(")")
        }
    }
}

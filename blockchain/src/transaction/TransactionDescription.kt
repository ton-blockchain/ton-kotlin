package org.ton.sdk.blockchain.transaction

import org.ton.sdk.blockchain.transaction.phases.*
import org.ton.tlb.CellRef
import kotlin.jvm.JvmName

/**
 * Detailed transaction info.
 *
 * @see [Transaction]
 */
public sealed class TransactionDescription {
    /**
     * Ordinary transaction info.
     */
    public class Ordinary(
        /**
         * Whether the credit phase was executed first
         *
         * (usually set when incoming message has `bounce: false`).
         */
        public val isCreditFirst: Boolean,

        /**
         * Storage phase info.
         *
         * Skipped if the account did not exist prior to execution.
         */
        @get:JvmName("storagePhase")
        public val storagePhase: StoragePhase?,

        /**
         * Credit phase info.
         *
         * Skipped if the incoming message is external.
         */
        @get:JvmName("creditPhase")
        public val creditPhase: CreditPhase?,

        /**
         * Compute phase info.
         */
        @get:JvmName("computePhase")
        public val computePhase: ComputePhase,

        /**
         * Action phase info.
         *
         * Skipped if the transaction was aborted at the compute phase.
         */
        @get:JvmName("actionPhase")
        public val actionPhase: ActionPhase?,

        /**
         * Whether the transaction was reverted.
         */
        public val isAborted: Boolean,

        /**
         * Bounce phase info.
         *
         * Only present if the incoming message had `bounce: true` and
         * the compute phase failed.
         */
        @get:JvmName("bouncePhase")
        public val bouncePhase: BouncePhase?,

        /**
         * Whether the account was destroyed during this transaction.
         */
        public val isDestroyed: Boolean
    ) : TransactionDescription() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Ordinary

            if (isCreditFirst != other.isCreditFirst) return false
            if (isAborted != other.isAborted) return false
            if (isDestroyed != other.isDestroyed) return false
            if (storagePhase != other.storagePhase) return false
            if (creditPhase != other.creditPhase) return false
            if (computePhase != other.computePhase) return false
            if (actionPhase != other.actionPhase) return false
            if (bouncePhase != other.bouncePhase) return false

            return true
        }

        override fun hashCode(): Int {
            var result = isCreditFirst.hashCode()
            result = 31 * result + isAborted.hashCode()
            result = 31 * result + isDestroyed.hashCode()
            result = 31 * result + (storagePhase?.hashCode() ?: 0)
            result = 31 * result + (creditPhase?.hashCode() ?: 0)
            result = 31 * result + computePhase.hashCode()
            result = 31 * result + (actionPhase?.hashCode() ?: 0)
            result = 31 * result + (bouncePhase?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = buildString {
            append("Ordinary(isCreditFirst=")
            append(isCreditFirst)
            append(", storagePhase=")
            append(storagePhase)
            append(", creditPhase=")
            append(creditPhase)
            append(", computePhase=")
            append(computePhase)
            append(", actionPhase=")
            append(actionPhase)
            append(", isAborted=")
            append(isAborted)
            append(", bouncePhase=")
            append(bouncePhase)
            append(", isDestroyed=")
            append(isDestroyed)
            append(")")
        }
    }

    /**
     * Tick-Tock transaction info
     */
    public class TickTock(
        public val isTock: Boolean,

        /**
         * Storage phase info.
         */
        @get:JvmName("storagePhase")
        public val storagePhase: StoragePhase,

        /**
         * Compute phase info.
         */
        @get:JvmName("computePhase")
        public val computePhase: ComputePhase,

        /**
         * Action phase info.
         *
         * Skipped if the transaction was aborted at the compute phase.
         */
        @get:JvmName("actionPhase")
        public val actionPhase: ActionPhase?,

        /**
         * Whether the transaction was reverted.
         */
        @get:JvmName("isAborted")
        public val isAborted: Boolean,

        /**
         * Whether the account was destroyed during this transaction.
         */
        public val isDestroyed: Boolean
    ) : TransactionDescription() {
        public val isTick: Boolean get() = !isTock

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as TickTock

            if (isTock != other.isTock) return false
            if (isAborted != other.isAborted) return false
            if (isDestroyed != other.isDestroyed) return false
            if (storagePhase != other.storagePhase) return false
            if (computePhase != other.computePhase) return false
            if (actionPhase != other.actionPhase) return false

            return true
        }

        override fun hashCode(): Int {
            var result = isTock.hashCode()
            result = 31 * result + isAborted.hashCode()
            result = 31 * result + isDestroyed.hashCode()
            result = 31 * result + storagePhase.hashCode()
            result = 31 * result + computePhase.hashCode()
            result = 31 * result + (actionPhase?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = buildString {
            append("TickTock(isTock=")
            append(isTock)
            append(", storagePhase=")
            append(storagePhase)
            append(", computePhase=")
            append(computePhase)
            append(", actionPhase=")
            append(actionPhase)
            append(", isAborted=")
            append(isAborted)
            append(", isDestroyed=")
            append(isDestroyed)
            append(")")
        }
    }

    /**
     * Storage transaction info
     *
     * **Currently, not implemented in TON Blockchain**
     */
    @Deprecated("Not implemented in TON Blockchain")
    public class Storage(
        /**
         * Storage phase info.
         */
        @get:JvmName("storagePhase")
        public val storagePhase: StoragePhase,
    ) : TransactionDescription()

    /**
     * Split prepare transaction info.
     *
     * **Currently, not implemented in TON Blockchain**
     */
    @Suppress("DEPRECATION")
    @Deprecated("Not implemented in TON Blockchain")
    public class SplitPrepare(
        @get:JvmName("splitInfo") public val splitInfo: SplitMergeInfo,
        @get:JvmName("storagePhase") public val storagePhase: StoragePhase?,
        @get:JvmName("computePhase") public val computePhase: ComputePhase,
        @get:JvmName("actionPhase") public val actionPhase: ActionPhase?,
        public val isAborted: Boolean,
        public val isDestroyed: Boolean
    ) : TransactionDescription()

    /**
     * Split install transaction info.
     *
     * **Currently, not implemented in TON Blockchain**
     */
    @Suppress("DEPRECATION")
    @Deprecated("Not implemented in TON Blockchain")
    public class SplitInstall(
        @get:JvmName("splitInfo") public val splitInfo: SplitMergeInfo,
        @get:JvmName("prepareTransaction") public val prepareTransaction: CellRef<Transaction>,
        public val isInstalled: Boolean,
    ) : TransactionDescription()

    /**
     * Merge-prepare transaction info.
     *
     * **Currently, not implemented in TON Blockchain**
     */
    @Suppress("DEPRECATION")
    @Deprecated("Not implemented in TON Blockchain")
    public class MergePrepare(
        @get:JvmName("splitInfo") public val splitInfo: SplitMergeInfo,
        @get:JvmName("storagePhase") public val storagePhase: StoragePhase,
        public val isAborted: Boolean,
    ) : TransactionDescription()

    /**
     * Merge-install transaction info.
     *
     * **Currently, not implemented in TON Blockchain**
     */
    @Suppress("DEPRECATION")
    @Deprecated("Not implemented in TON Blockchain")
    public class MergeInstall(
        @get:JvmName("splitInfo") public val splitInfo: SplitMergeInfo,
        @get:JvmName("prepareTransaction") public val prepareTransaction: CellRef<Transaction>,
        @get:JvmName("storagePhase") public val storagePhase: StoragePhase?,
        @get:JvmName("creditPhase") public val creditPhase: CreditPhase?,
        @get:JvmName("computePhase") public val computePhase: ComputePhase,
        @get:JvmName("actionPhase") public val actionPhase: ActionPhase?,
        @get:JvmName("isAborted") public val isAborted: Boolean,
        @get:JvmName("isDestroyed") public val isDestroyed: Boolean
    ) : TransactionDescription()
}

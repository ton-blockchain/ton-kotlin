package org.ton.sdk.blockchain.transaction

import org.ton.sdk.blockchain.transaction.phases.*
import org.ton.tlb.CellRef

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
        public val storagePhase: StoragePhase?,

        /**
         * Credit phase info.
         *
         * Skipped if the incoming message is external.
         */
        public val creditPhase: CreditPhase?,

        /**
         * Compute phase info.
         */
        public val computePhase: ComputePhase,

        /**
         * Action phase info.
         *
         * Skipped if the transaction was aborted at the compute phase.
         */
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
        public val bouncePhase: BouncePhase?,

        /**
         * Whether the account was destroyed during this transaction.
         */
        public val isDestroyed: Boolean
    ) : TransactionDescription()

    /**
     * Tick-Tock transaction info
     */
    public class TickTock(
        public val isTock: Boolean,

        /**
         * Storage phase info.
         */
        public val storagePhase: StoragePhase,

        /**
         * Compute phase info.
         */
        public val computePhase: ComputePhase,

        /**
         * Action phase info.
         *
         * Skipped if the transaction was aborted at the compute phase.
         */
        public val actionPhase: ActionPhase?,

        /**
         * Whether the transaction was reverted.
         */
        public val isAborted: Boolean,

        /**
         * Whether the account was destroyed during this transaction.
         */
        public val isDestroyed: Boolean
    ) : TransactionDescription() {
        public val isTick: Boolean get() = !isTock
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
        public val splitInfo: SplitMergeInfo,
        public val storagePhase: StoragePhase?,
        public val computePhase: ComputePhase,
        public val actionPhase: ActionPhase?,
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
        public val splitInfo: SplitMergeInfo,
        public val prepareTransaction: CellRef<Transaction>,
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
        public val splitInfo: SplitMergeInfo,
        public val storagePhase: StoragePhase,
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
        public val splitInfo: SplitMergeInfo,
        public val prepareTransaction: CellRef<Transaction>,
        public val storagePhase: StoragePhase?,
        public val creditPhase: CreditPhase?,
        public val computePhase: ComputePhase,
        public val actionPhase: ActionPhase?,
        public val isAborted: Boolean,
        public val isDestroyed: Boolean
    ) : TransactionDescription()
}

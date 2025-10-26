@file:Suppress("OPT_IN_USAGE", "PackageDirectoryMismatch")

package org.ton.kotlin.transaction

import org.ton.bigint.toInt
import org.ton.block.SplitMergeInfo
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.storeRef
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.transaction.phase.*
import org.ton.tlb.CellRef
import org.ton.tlb.NullableTlbCodec
import org.ton.tlb.TlbCodec

/**
 * Detailed transaction info.
 *
 * @see [Transaction]
 */
public sealed interface TransactionInfo {

    /**
     * Ordinary transaction info.
     */
    public data class Ordinary(
        /**
         * Whether the credit phase was executed first
         *
         * (usually set when incoming message has `bounce: false`).
         */
        val isCreditFirst: Boolean,

        /**
         * Storage phase info.
         *
         * Skipped if the account did not exist prior to execution.
         */
        val storagePhase: StoragePhase?,

        /**
         * Credit phase info.
         *
         * Skipped if the incoming message is external.
         */
        val creditPhase: CreditPhase?,

        /**
         * Compute phase info.
         */
        val computePhase: ComputePhase,

        /**
         * Action phase info.
         *
         * Skipped if the transaction was aborted at the compute phase.
         */
        val actionPhase: ActionPhase?,

        /**
         * Whether the transaction was reverted.
         */
        val isAborted: Boolean,

        /**
         * Bounce phase info.
         *
         * Only present if the incoming message had `bounce: true` and
         * the compute phase failed.
         */
        val bouncePhase: BouncePhase?,

        /**
         * Whether the account was destroyed during this transaction.
         */
        val isDestroyed: Boolean
    ) : TransactionInfo

    /**
     * Tick-Tock transaction info
     */
    public data class TickTock(
        val isTock: Boolean,

        /**
         * Storage phase info.
         */
        val storagePhase: StoragePhase,

        /**
         * Compute phase info.
         */
        val computePhase: ComputePhase,

        /**
         * Action phase info.
         *
         * Skipped if the transaction was aborted at the compute phase.
         */
        val actionPhase: ActionPhase?,

        /**
         * Whether the transaction was reverted.
         */
        val isAborted: Boolean,

        /**
         * Whether the account was destroyed during this transaction.
         */
        val isDestroyed: Boolean
    ) : TransactionInfo {
        val isTick: Boolean get() = !isTock
    }

    /**
     * Storage transaction info
     *
     * **Currently, not implemented in TON Blockchain**
     */
    @Deprecated("Not implemented in TON Blockchain")
    public data class Storage(
        /**
         * Storage phase info.
         */
        val storagePhase: StoragePhase,
    ) : TransactionInfo

    /**
     * Split prepare transaction info.
     *
     * **Currently, not implemented in TON Blockchain**
     */
    @Deprecated("Not implemented in TON Blockchain")
    public data class SplitPrepare(
        val splitInfo: SplitMergeInfo,
        val storagePhase: StoragePhase?,
        val computePhase: ComputePhase,
        val actionPhase: ActionPhase?,
        val isAborted: Boolean,
        val isDestroyed: Boolean
    ) : TransactionInfo

    /**
     * Split install transaction info.
     *
     * **Currently, not implemented in TON Blockchain**
     */
    @Deprecated("Not implemented in TON Blockchain")
    public data class SplitInstall(
        val splitInfo: SplitMergeInfo,
        val prepareTransaction: CellRef<Transaction>,
        val isInstalled: Boolean,
    ) : TransactionInfo

    /**
     * Merge-prepare transaction info.
     *
     * **Currently, not implemented in TON Blockchain**
     */
    @Deprecated("Not implemented in TON Blockchain")
    public data class MergePrepare(
        val splitInfo: SplitMergeInfo,
        val storagePhase: StoragePhase,
        val isAborted: Boolean,
    ) : TransactionInfo

    /**
     * Merge-install transaction info.
     *
     * **Currently, not implemented in TON Blockchain**
     */
    @Deprecated("Not implemented in TON Blockchain")
    public data class MergeInstall(
        val splitInfo: SplitMergeInfo,
        val prepareTransaction: CellRef<Transaction>,
        val storagePhase: StoragePhase?,
        val creditPhase: CreditPhase?,
        val computePhase: ComputePhase,
        val actionPhase: ActionPhase?,
        val isAborted: Boolean,
        val isDestroyed: Boolean
    ) : TransactionInfo

    public companion object : TlbCodec<TransactionInfo> by TransactionInfoCodec
}

private object TransactionInfoCodec : TlbCodec<TransactionInfo> {
    private val maybeStoragePhase = NullableTlbCodec(StoragePhase)
    private val maybeCreditPhase = NullableTlbCodec(CreditPhase)
    private val maybeBouncePhase = NullableTlbCodec(BouncePhase)

    private fun CellBuilder.storeMaybeRefActionPhase(actionPhase: ActionPhase?, context: CellContext) {
        if (actionPhase != null) {
            storeBoolean(true)
            storeRef(context) {
                ActionPhase.storeTlb(this, actionPhase, context)
            }
        } else {
            storeBoolean(false)
        }
    }

    private fun CellSlice.loadMaybeRefActionPhase(context: CellContext): ActionPhase? {
        return if (loadBoolean()) {
            ActionPhase.loadTlb(context.loadCell(loadRef()).beginParse(), context)
        } else {
            null
        }
    }

    @Suppress("DEPRECATION")
    override fun storeTlb(builder: CellBuilder, value: TransactionInfo, context: CellContext) {
        when (value) {
            is TransactionInfo.Ordinary -> { // trans_ord$0000
                builder.storeUInt(0b0000, 4)
                builder.storeBoolean(value.isCreditFirst)
                maybeStoragePhase.storeTlb(builder, value.storagePhase, context)
                maybeCreditPhase.storeTlb(builder, value.creditPhase, context)
                ComputePhase.storeTlb(builder, value.computePhase, context)
                builder.storeMaybeRefActionPhase(value.actionPhase, context)
                builder.storeBoolean(value.isAborted)
                maybeBouncePhase.storeTlb(builder, value.bouncePhase, context)
                builder.storeBoolean(value.isDestroyed)
            }

            is TransactionInfo.TickTock -> {
                builder.storeUInt(if (value.isTock) 0b0001 else 0b0000, 4)
                StoragePhase.storeTlb(builder, value.storagePhase, context)
                ComputePhase.storeTlb(builder, value.computePhase, context)

                builder.storeBoolean(value.isAborted)
                builder.storeBoolean(value.isDestroyed)
            }

            is TransactionInfo.SplitPrepare -> {
                builder.storeUInt(0b1000, 4)
                SplitMergeInfo.storeTlb(builder, value.splitInfo, context)
                maybeStoragePhase.storeTlb(builder, value.storagePhase, context)
                ComputePhase.storeTlb(builder, value.computePhase, context)
                builder.storeMaybeRefActionPhase(value.actionPhase, context)
                builder.storeBoolean(value.isAborted)
                builder.storeBoolean(value.isDestroyed)
            }

            is TransactionInfo.SplitInstall -> {
                builder.storeUInt(0b0101, 4)
                SplitMergeInfo.storeTlb(builder, value.splitInfo, context)
                builder.storeRef(value.prepareTransaction.cell)
                builder.storeBoolean(value.isInstalled)
            }

            is TransactionInfo.MergePrepare -> {
                builder.storeUInt(0b0110, 4)
                SplitMergeInfo.storeTlb(builder, value.splitInfo, context)
                StoragePhase.storeTlb(builder, value.storagePhase, context)
                builder.storeBoolean(value.isAborted)
            }

            is TransactionInfo.MergeInstall -> {
                builder.storeUInt(0b0111, 4)
                SplitMergeInfo.storeTlb(builder, value.splitInfo, context)
                builder.storeRef(value.prepareTransaction.cell)
                maybeStoragePhase.storeTlb(builder, value.storagePhase, context)
                maybeCreditPhase.storeTlb(builder, value.creditPhase, context)
                ComputePhase.storeTlb(builder, value.computePhase, context)
                builder.storeMaybeRefActionPhase(value.actionPhase, context)
                builder.storeBoolean(value.isAborted)
                builder.storeBoolean(value.isDestroyed)
            }

            is TransactionInfo.Storage -> {
                builder.storeUInt(0b0001, 4)
                StoragePhase.storeTlb(builder, value.storagePhase, context)
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun loadTlb(slice: CellSlice, context: CellContext): TransactionInfo {
        when (val tag = getTag(slice)) {
            0b0000 -> { // trans_ord$0000
                slice.bitsPosition += 4
                val isCreditFirst = slice.loadBoolean()
                val storagePhase = maybeStoragePhase.loadTlb(slice, context)
                val creditPhase = maybeCreditPhase.loadTlb(slice, context)
                val computePhase = ComputePhase.loadTlb(slice, context)
                val actionPhase = slice.loadMaybeRefActionPhase(context)
                val isAborted = slice.loadBoolean()
                val bouncePhase = maybeBouncePhase.loadTlb(slice, context)
                val isDestroyed = slice.loadBoolean()
                return TransactionInfo.Ordinary(
                    isCreditFirst,
                    storagePhase,
                    creditPhase,
                    computePhase,
                    actionPhase,
                    isAborted,
                    bouncePhase,
                    isDestroyed
                )
            }

            0b0001 -> { // trans_storage$0001
                slice.bitsPosition += 4
                val storagePhase = StoragePhase.loadTlb(slice, context)
                return TransactionInfo.Storage(storagePhase)
            }

            0b0010 -> { // trans_tick_tock$001
                slice.bitsPosition += 3
                val isTock = slice.loadBoolean()
                val storagePhase = StoragePhase.loadTlb(slice, context)
                val computePhase = ComputePhase.loadTlb(slice, context)
                val actionPhase = slice.loadMaybeRefActionPhase(context)
                val isAborted = slice.loadBoolean()
                val isDestroyed = slice.loadBoolean()
                return TransactionInfo.TickTock(isTock, storagePhase, computePhase, actionPhase, isAborted, isDestroyed)
            }

            0b0100 -> { // trans_split_prepare$0100
                slice.bitsPosition += 4
                val splitInfo = SplitMergeInfo.loadTlb(slice, context)
                val storagePhase = maybeStoragePhase.loadTlb(slice, context)
                val computePhase = ComputePhase.loadTlb(slice, context)
                val actionPhase = slice.loadMaybeRefActionPhase(context)
                val isAborted = slice.loadBoolean()
                val isDestroyed = slice.loadBoolean()
                return TransactionInfo.SplitPrepare(
                    splitInfo,
                    storagePhase,
                    computePhase,
                    actionPhase,
                    isAborted,
                    isDestroyed
                )
            }

            0b0101 -> { // trans_split_install$0101
                slice.bitsPosition += 4
                val splitInfo = SplitMergeInfo.loadTlb(slice, context)
                val prepareTransaction = CellRef(slice.loadRef(), Transaction)
                val isInstalled = slice.loadBoolean()
                return TransactionInfo.SplitInstall(splitInfo, prepareTransaction, isInstalled)
            }

            0b0110 -> { // trans_merge_prepare$0110
                slice.bitsPosition += 4
                val splitInfo = SplitMergeInfo.loadTlb(slice, context)
                val storagePhase = StoragePhase.loadTlb(slice, context)
                val isAborted = slice.loadBoolean()
                return TransactionInfo.MergePrepare(splitInfo, storagePhase, isAborted)
            }

            0b0111 -> { // trans_merge_install$0111
                slice.bitsPosition += 4
                val splitInfo = SplitMergeInfo.loadTlb(slice, context)
                val prepareTransaction = CellRef(slice.loadRef(), Transaction)
                val storagePhase = maybeStoragePhase.loadTlb(slice, context)
                val creditPhase = maybeCreditPhase.loadTlb(slice, context)
                val computePhase = ComputePhase.loadTlb(slice, context)
                val actionPhase = slice.loadMaybeRefActionPhase(context)
                val isAborted = slice.loadBoolean()
                val isDestroyed = slice.loadBoolean()
                return TransactionInfo.MergeInstall(
                    splitInfo,
                    prepareTransaction,
                    storagePhase,
                    creditPhase,
                    computePhase,
                    actionPhase,
                    isAborted,
                    isDestroyed
                )
            }

            else -> throw IllegalArgumentException("Invalid transaction info tag: $tag")
        }
    }

    private fun getTag(slice: CellSlice): Int = slice.bitSelect(4, 0xF7)

    private fun CellSlice.bitSelect(bitCount: Int, mask: Int): Int {
        val n = preloadUInt(bitCount).toInt()
        return (mask and ((0b10 shl n) - 1)).countOneBits() - 1
    }
}

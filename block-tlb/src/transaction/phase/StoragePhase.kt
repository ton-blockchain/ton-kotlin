@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.transaction.phase

import org.ton.kotlin.block.AccStatusChange
import org.ton.kotlin.block.Coins
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.NullableTlbCodec
import org.ton.kotlin.tlb.TlbCodec

/**
 * Storage phase info.
 *
 * At this phase, the account pays for storing its state.
 *
 * @see [org.ton.kotlin.transaction.TransactionInfo]
 */
public data class StoragePhase(
    /**
     * Amount of coins collected for storing this contract for some time.
     */
    val storageFeesCollected: Coins,

    /**
     * Amount of coins which this account owes to the network
     */
    val storageFeesDue: Coins?,

    /**
     *  Account status change during execution of this phase.
     */
    val statusChange: AccStatusChange
) {
    public companion object : TlbCodec<StoragePhase> by TrStoragePhaseTlbConstructor
}

private object TrStoragePhaseTlbConstructor : TlbCodec<StoragePhase> {
    val maybeCoins = NullableTlbCodec(Coins)

    override fun storeTlb(
        builder: CellBuilder,
        value: StoragePhase,
        context: CellContext
    ) {
        Coins.storeTlb(builder, value.storageFeesCollected, context)
        maybeCoins.storeTlb(builder, value.storageFeesDue, context)
        AccStatusChange.storeTlb(builder, value.statusChange, context)
    }

    override fun loadTlb(
        slice: CellSlice,
        context: CellContext
    ): StoragePhase {
        val storageFeesCollected = Coins.loadTlb(slice, context)
        val storageFeesDue = maybeCoins.loadTlb(slice, context)
        val statusChange = AccStatusChange.loadTlb(slice, context)
        return StoragePhase(storageFeesCollected, storageFeesDue, statusChange)
    }
}

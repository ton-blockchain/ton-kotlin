package org.ton.kotlin.account

import org.ton.kotlin.block.Coins
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbCodec

/**
 * Storage profile of an [Account].
 */
public data class StorageInfo(
    /**
     * Number of unique cells and bits which account state occupies.
     */
    val used: StorageUsed,

    /**
     * Extra information about storage.
     */
    val extra: StorageExtraInfo?,

    /**
     * Unix timestamp in seconds of the last storage phase.
     */
    val lastPaid: Long,

    /**
     * Account debt for storing its state.
     */
    val duePayment: Coins?
) {
    public companion object : TlbCodec<StorageInfo> by StorageInfoTlbCodec
}

private object StorageInfoTlbCodec : TlbCodec<StorageInfo> {
    override fun loadTlb(slice: CellSlice, context: CellContext): StorageInfo {
        val used = StorageUsed.loadTlb(slice, context)
        val extra = StorageExtraInfo.loadTlb(slice, context)
        val lastPaid = slice.loadLong(32)
        val duePayment = if (slice.loadBoolean()) Coins.loadTlb(slice, context) else null
        return StorageInfo(used, extra, lastPaid, duePayment)
    }

    override fun storeTlb(builder: org.ton.kotlin.cell.CellBuilder, value: StorageInfo, context: CellContext) {
        StorageUsed.storeTlb(builder, value.used, context)
        StorageExtraInfo.storeTlb(builder, value.extra, context)
        builder.storeLong(value.lastPaid, 32)
        if (value.duePayment == null) {
            builder.storeBoolean(false)
        } else {
            builder.storeBoolean(true)
            Coins.storeTlb(builder, value.duePayment, context)
        }
    }
}

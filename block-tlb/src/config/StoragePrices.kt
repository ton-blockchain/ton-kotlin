package org.ton.kotlin.block.config

import org.ton.kotlin.bigint.*
import org.ton.kotlin.block.Coins
import org.ton.kotlin.block.StorageUsedShort
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbCodec

/**
 * Storage prices for some interval.
 */
public data class StoragePrices(
    /**
     * Unix timestamp in seconds since which this prices are used.
     */
    val validSince: Long,

    /**
     * Bit price in base workchain.
     */
    val bitPrice: Long,

    /**
     * Cell price in base workchain.
     */
    val cellPrice: Long,

    /**
     * Bit price in masterchain.
     */
    val mcBitPrice: Long,

    /**
     * Cell price in masterchain.
     */
    val mcCellPrice: Long,
) {
    /**
     * Computes the amount of fees for storing [stats] data for [delta] seconds.
     */
    public fun computeStorageFee(
        isMasterchain: Boolean,
        delta: Long,
        stats: StorageUsedShort
    ): Coins {
        var result = if (isMasterchain) {
            (stats.cellCount.toBigInt().times(mcCellPrice.toBigInt())).plus(
                (stats.bitCount.toBigInt().times(mcBitPrice.toBigInt()))
            )
        } else {
            (stats.cellCount.toBigInt().times(cellPrice.toBigInt())).plus(
                (stats.bitCount.toBigInt().times(bitPrice.toBigInt()))
            )
        }
        result = result.times(delta.toBigInt())
        val r = result.and(0xFFFF.toBigInt()).toLong() != 0L
        result = result.shr(16)
        if (r) {
            result = result.plus(1.toBigInt())
        }
        return Coins(result)
    }

    public companion object : TlbCodec<StoragePrices> by StoragePricesTlbCodec
}

private object StoragePricesTlbCodec : TlbCodec<StoragePrices> {
    override fun loadTlb(slice: CellSlice, context: CellContext): StoragePrices {
        val tag = slice.loadUInt(8).toInt()
        require(tag == 0xCC) { "Invalid StorageUsedShort tag: ${tag.toHexString()}" }
        val validSince = slice.loadUInt(32).toLong()
        val bitPrice = slice.loadULong().toLong()
        val cellPrice = slice.loadULong().toLong()
        val mcBitPrice = slice.loadULong().toLong()
        val mcCellPrice = slice.loadULong().toLong()
        return StoragePrices(validSince, bitPrice, cellPrice, mcBitPrice, mcCellPrice)
    }

    override fun storeTlb(builder: CellBuilder, value: StoragePrices, context: CellContext) {
        builder.storeUInt(0xCC, 8)
        builder.storeUInt(value.validSince, 32)
        builder.storeULong(value.bitPrice.toULong(), 64)
        builder.storeULong(value.cellPrice.toULong(), 64)
        builder.storeULong(value.mcBitPrice.toULong(), 64)
        builder.storeULong(value.mcCellPrice.toULong(), 64)
    }
}

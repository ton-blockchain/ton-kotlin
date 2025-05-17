package org.ton.kotlin.account

import org.ton.kotlin.bigint.toBigInt
import org.ton.kotlin.block.VarUInteger
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbCodec

/**
 * Number of unique cells and bits for shard states.
 */
public data class StorageUsed(
    /**
     * Number of unique cells.
     */
    val cells: Long,

    /**
     * The total number of bits in unique cells.
     */
    val bits: Long,
) {
    public companion object : TlbCodec<StorageUsed> by StorageUsedTlbCodec {
        public val ZERO: StorageUsed = StorageUsed(0, 0)
    }
}

private object StorageUsedTlbCodec : TlbCodec<StorageUsed> {
    private val varUInt7 = VarUInteger.tlbCodec(7)

    override fun loadTlb(slice: CellSlice, context: CellContext): StorageUsed {
        val cells = varUInt7.loadTlb(slice, context).value.toLong()
        val bits = varUInt7.loadTlb(slice, context).value.toLong()
        return StorageUsed(cells, bits)
    }

    @Suppress("DEPRECATION")
    override fun storeTlb(builder: CellBuilder, value: StorageUsed, context: CellContext) {
        varUInt7.storeTlb(builder, VarUInteger(value.cells.toBigInt()), context)
        varUInt7.storeTlb(builder, VarUInteger(value.bits.toBigInt()), context)
    }
}

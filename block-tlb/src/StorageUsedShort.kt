package org.ton.kotlin.block

import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSize
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb

/**
 * Cell tree storage stats.
 */
public data class StorageUsedShort(
    /**
     * Total number of cells in tree.
     */
    val cellCount: Long,

    /**
     * Total number of bits in tree.
     */
    val bitCount: Long
) {
    public constructor(cellSize: CellSize) : this(cellSize.minRefs.toLong(), cellSize.minBits.toLong()) {
        require(cellSize.isFixed()) { "Cell size must be fixed" }
    }

    public fun toCellSize(): CellSize = CellSize(bitCount.toInt(), cellCount.toInt())

    public operator fun plus(other: StorageUsedShort): StorageUsedShort =
        StorageUsedShort(cellCount + other.cellCount, bitCount + other.bitCount)

    public companion object : TlbConstructorProvider<StorageUsedShort> by StorageUsedShortTlbConstructor {
        public val ZERO: StorageUsedShort = StorageUsedShort(0, 0)
    }
}

private object StorageUsedShortTlbConstructor : TlbConstructor<StorageUsedShort>(
    schema = "storage_used_short\$_ cells:(VarUInteger 7) bits:(VarUInteger 7) = StorageUsedShort;"
) {
    private val varUInteger7Codec = VarUInteger.tlbCodec(7)

    @Suppress("DEPRECATION")
    override fun storeTlb(
        cellBuilder: CellBuilder, value: StorageUsedShort
    ) = cellBuilder {
        storeTlb(varUInteger7Codec, VarUInteger(value.cellCount))
        storeTlb(varUInteger7Codec, VarUInteger(value.bitCount))
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): StorageUsedShort = cellSlice {
        val cells = loadTlb(varUInteger7Codec).value.toLong()
        val bits = loadTlb(varUInteger7Codec).value.toLong()
        StorageUsedShort(cells, bits)
    }
}

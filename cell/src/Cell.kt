package org.ton.sdk.cell

import org.ton.sdk.cell.internal.EmptyCell
import org.ton.sdk.crypto.HashBytes
import kotlin.jvm.JvmStatic

public interface Cell {
    /**
     * Provides metadata about a cell, encapsulated as a [CellDescriptor].
     */
    public val descriptor: CellDescriptor

    public val cellType: CellType get() = descriptor.cellType

    public val level: Int get() = descriptor.levelMask.level

    public val levelMask: LevelMask get() = descriptor.levelMask

    public val referenceCount: Int get() = descriptor.referenceCount

    public val isExotic: Boolean get() = descriptor.isExotic

    /**
     * Returns this cell as a virtualized cell, so that all hashes
     * and depths will have an offset.
     */
    public fun virtualize(offset: Int): Cell

    /**
     * Returns the hash of the current cell for the specified level.
     *
     * @param level the hierarchy level of the cell for which the hash is to be computed
     * @return the calculated hash as an instance of [HashBytes] for the specified level
     */
    public fun hash(level: Int = LevelMask.MAX_LEVEL): HashBytes

    /**
     * Returns the depth of a cell for the specified level.
     *
     * @param level the level for which the depth needs to be calculated
     * @return the depth of the cell at the specified level
     */
    public fun depth(level: Int = LevelMask.MAX_LEVEL): Int

    /**
     * Checks whether the cell is empty.
     *
     * @return `true` if the cell is empty, `false` otherwise
     */
    public fun isEmpty(): Boolean = hash(LevelMask.MAX_LEVEL) == EmptyCell.EMPTY_CELL_HASH

    public companion object {
        public const val MAX_BIT_LENGHT: Int = 1023

        @JvmStatic
        public val EMPTY: Cell = EmptyCell
    }
}

public interface LoadedCell : Cell {
    public fun reference(index: Int): Cell?
}

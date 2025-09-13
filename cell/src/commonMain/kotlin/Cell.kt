package org.ton.kotlin.cell

import kotlinx.io.bytestring.ByteString

public sealed interface Cell {
    public val descriptor: CellDescriptor
    public val cellType: CellType get() = descriptor.cellType
    public val levelMask: LevelMask get() = descriptor.levelMask
    public val level: Int get() = levelMask.level

    public fun hash(level: Int = MAX_LEVEL): ByteString

    public fun depth(level: Int = MAX_LEVEL): Int

    public companion object {
        public const val HASH_BYTES: Int = 32
        public const val DEPTH_BYTES: Int = 2
        public const val MAX_LEVEL: Int = 3
        public const val MAX_BITS: Int = 1023
        public const val MAX_REFS: Int = 4
    }
}

public interface LoadedCell : Cell {
    public val references: List<Cell>
}

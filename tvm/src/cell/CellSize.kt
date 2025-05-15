@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.cell

public class CellSize(
    public val minBits: Int,
    public val minRefs: Int,
    public val maxBits: Int,
    public val maxRefs: Int
) {
    public constructor(bits: Int, refs: Int) : this(bits, refs, bits, refs)

    public fun isFixed(): Boolean = minBits == maxBits && minRefs == maxRefs

    public fun fitsIntoCell(): Boolean {
        return minBits in 0..Cell.MAX_BITS_SIZE && maxBits in 0..Cell.MAX_BITS_SIZE && minRefs in 0..4 && maxRefs in 0..4
    }

    public operator fun plus(other: CellSize): CellSize =
        CellSize(
            minBits = minBits + other.minBits,
            minRefs = minRefs + other.minRefs,
            maxBits = maxBits + other.maxBits,
            maxRefs = maxRefs + other.maxRefs
        )

    override fun toString(): String = buildString {
        fun appendSize(bits: Int, refs: Int) {
            append(bits)
            if (refs > 0) {
                append("+")
                append(refs)
                append("R")
            }
        }
        if (isFixed()) {
            append("=")
            appendSize(minBits, minRefs)
        } else {
            appendSize(minBits, minRefs)
            append("..")
            appendSize(maxBits, maxRefs)
        }
    }

    public companion object {
        public val ZERO: CellSize = CellSize(0, 0, 0, 0)
    }
}

public interface CellSizeable {
    public val cellSize: CellSize
}

public val CellSizeable?.cellSize: CellSize get() = this?.cellSize ?: CellSize.ZERO

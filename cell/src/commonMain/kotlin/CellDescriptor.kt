package org.ton.kotlin.cell

import kotlin.jvm.JvmStatic

public data class CellDescriptor(
    /**
     * First descriptor byte with a generic info about cell.
     */
    public val d1: Byte,

    /**
     * Second descriptor byte with a packed data size.
     */
    public val d2: Byte,
) {
    /**
     * [LevelMask] encoded in [d1].
     */
    public val levelMask: LevelMask get() = LevelMask(d1.toInt() ushr 5)

    /**
     * Returns whether cell contains hashes in data.
     */
    public val hasHashes: Boolean get() = d1.toInt() and HAS_HASHES_MASK != 0

    /**
     * Returns whether the cell is not [CellType.ORDINARY]
     */
    public val isExotic: Boolean get() = d1.toInt() and IS_EXOTIC_MASK != 0

    /**
     * Reference count encoded in [d1].
     */
    public val referenceCount: Int get() = d1.toInt() and REFERENCE_COUNT_MASK

    /**
     * Returns whether cell refers to some external data.
     */
    public val isAbsent: Boolean get() = d1.toInt() == (IS_EXOTIC_MASK or REFERENCE_COUNT_MASK)

    /**
     * Returns whether cell data is 8-bit aligned.
     */
    public val isAligned: Boolean get() = d2.toInt() and 1 == 0

    /**
     * Cell data length in bytes.
     */
    public val dataLength: Int
        get() {
            val d2 = d2.toInt() and 0xFF
            return (d2 and 1) + (d2 ushr 1)
        }

    /**
     * Calculated cell type.
     */
    public val cellType: CellType
        get() {
            val d1 = d1.toInt()
            return if (d1 and IS_EXOTIC_MASK == 0) {
                CellType.ORDINARY
            } else when (d1 and REFERENCE_COUNT_MASK) {
                0 -> {
                    if (d1 and LEVEL_MASK == 0) CellType.LIBRARY_REFERENCE
                    else CellType.PRUNED_BRANCH
                }

                1 -> CellType.MERKLE_PROOF
                else -> CellType.MERKLE_UPDATE
            }
        }

    /**
     * Calculated hash count.
     */
    public val hashCount: Int
        get() {
            val level = levelMask.level
            return if (isExotic && referenceCount == 0 && level > 0) {
                1 // pruned branch always has 1 hash
            } else {
                level + 1
            }
        }

    override fun hashCode(): Int = (d1.toInt() shl 8) or d2.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as CellDescriptor
        if (d1 != other.d1) return false
        if (d2 != other.d2) return false
        return true
    }

    public companion object {
        public const val LEVEL_MASK: Int = 0b1110_0000
        public const val HAS_HASHES_MASK: Int = 0b0001_0000
        public const val IS_EXOTIC_MASK: Int = 0b0000_1000
        public const val REFERENCE_COUNT_MASK: Int = 0b0000_0111

        @JvmStatic
        public fun computeD1(
            levelMask: LevelMask,
            hasHashes: Boolean,
            isExotic: Boolean,
            referenceCount: Int
        ): Byte {
            var d1 = levelMask.mask shl 5
            d1 = d1 or (if (isExotic) IS_EXOTIC_MASK else 0)
            d1 = d1 or (if (hasHashes) HAS_HASHES_MASK else 0)
            d1 = d1 or (referenceCount and REFERENCE_COUNT_MASK)
            return d1.toByte()
        }

        @JvmStatic
        public fun computeD2(bitLength: Int): Byte {
            var d2 = (bitLength ushr 2) and 1.inv()
            d2 = d2 or if (bitLength % 8 != 0) 1 else 0
            return d2.toByte()
        }
    }
}

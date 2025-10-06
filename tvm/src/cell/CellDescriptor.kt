package org.ton.cell

import org.ton.cell.CellDescriptor.Companion.HAS_HASHES_MASK
import org.ton.cell.CellDescriptor.Companion.IS_EXOTIC_MASK
import org.ton.cell.CellDescriptor.Companion.LEVEL_MASK
import org.ton.cell.CellDescriptor.Companion.REFERENCE_COUNT_MASK
import kotlin.jvm.JvmStatic

public interface CellDescriptor {
    /**
     * First descriptor byte with a generic info about cell.
     */
    public val d1: Byte

    /**
     * Second descriptor byte with a packed data size.
     */
    public val d2: Byte

    /**
     * [LevelMask] encoded in [d1].
     */
    public val levelMask: LevelMask

    /**
     * Returns whether cell contains hashes in data.
     */
    public val hasHashes: Boolean

    /**
     * Returns whether the cell is not [CellType.ORDINARY]
     */
    public val isExotic: Boolean

    /**
     * Reference count encoded in [d1].
     */
    public val referenceCount: Int

    /**
     * Returns whether cell refers to some external data.
     */
    public val isAbsent: Boolean

    /**
     * Returns whether cell data is 8-bit aligned.
     */
    public val isAligned: Boolean

    /**
     * Cell data length in bytes.
     */
    public val dataLength: Int

    /**
     * Calculated cell type.
     */
    public val cellType: CellType

    /**
     * Calculated hash count.
     */
    public val hashCount: Int

    public operator fun component1(): Byte = d1
    public operator fun component2(): Byte = d2

    public companion object {
        public const val LEVEL_MASK: Int = 0b1110_0000
        public const val HAS_HASHES_MASK: Int = 0b0001_0000
        public const val IS_EXOTIC_MASK: Int = 0b0000_1000
        public const val REFERENCE_COUNT_MASK: Int = 0b0000_0111

        @JvmStatic
        public fun computeD1(levelMask: LevelMask, isExotic: Boolean, referenceCount: Int): Byte {
            var d1 = levelMask.mask shl 5
            d1 = d1 or ((if (isExotic) 1 else 0) shl 3)
            d1 = d1 or (referenceCount and REFERENCE_COUNT_MASK)
            return d1.toByte()
        }

        @JvmStatic
        public fun computeD2(bitLength: Int): Byte {
            var d2 = (bitLength ushr 2) and 1.inv()
            d2 = d2 or if (bitLength % 8 != 0) 1 else 0
            return d2.toByte()
        }

        @JvmStatic
        public fun fromBytes(source: ByteArray): CellDescriptor =
            CellDescriptorImpl(source[0], source[1])

        @JvmStatic
        public fun fromBytes(source: ByteArray, startIndex: Int): CellDescriptor =
            CellDescriptorImpl(source[startIndex], source[startIndex + 1])

        @JvmStatic
        public fun fromBytes(d1: Byte, d2: Byte): CellDescriptor =
            CellDescriptorImpl(d1, d2)

        @JvmStatic
        public fun from(levelMask: LevelMask, isExotic: Boolean, referenceCount: Int, bitLength: Int): CellDescriptor =
            CellDescriptorImpl(computeD1(levelMask, isExotic, referenceCount), computeD2(bitLength))
    }
}

@Suppress("NOTHING_TO_INLINE")
public inline fun CellDescriptor(source: ByteArray): CellDescriptor =
    CellDescriptor.fromBytes(source)

@Suppress("NOTHING_TO_INLINE")
public inline fun CellDescriptor(source: ByteArray, startIndex: Int): CellDescriptor =
    CellDescriptor.fromBytes(source, startIndex)

@Suppress("NOTHING_TO_INLINE")
public inline fun CellDescriptor(d1: Byte, d2: Byte): CellDescriptor =
    CellDescriptor.fromBytes(d1, d2)

@Suppress("NOTHING_TO_INLINE")
public inline fun CellDescriptor(
    levelMask: LevelMask,
    isExotic: Boolean,
    referenceCount: Int,
    bitLength: Int
): CellDescriptor =
    CellDescriptor.from(levelMask, isExotic, referenceCount, bitLength)

private data class CellDescriptorImpl(
    override val d1: Byte,
    override val d2: Byte
) : CellDescriptor {
    override val levelMask: LevelMask
        get() = LevelMask(d1.toInt() ushr 5)

    override val isExotic: Boolean
        get() = d1.toInt() and IS_EXOTIC_MASK != 0

    override val hasHashes: Boolean
        get() = d1.toInt() and HAS_HASHES_MASK != 0

    override val referenceCount: Int
        get() = d1.toInt() and REFERENCE_COUNT_MASK

    override val isAbsent: Boolean
        get() = d1.toInt() == (IS_EXOTIC_MASK or REFERENCE_COUNT_MASK)

    override val isAligned: Boolean
        get() = d2.toInt() and 1 == 0

    override val dataLength: Int
        get() {
            val d2 = d2.toInt() and 0xFF
            return (d2 and 1) + (d2 ushr 1)
        }

    override val cellType: CellType
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

    override val hashCount: Int
        get() {
            val level = levelMask.level
            return if (isExotic && referenceCount == 0 && level > 0) {
                1 // pruned branch always has 1 hash
            } else {
                level + 1
            }
        }

    override fun toString(): String =
        "CellDescriptor(d1=${d1.toString(2).padStart(8, '0')}, d2=${d2.toString(2).padStart(8, '0')})"
}

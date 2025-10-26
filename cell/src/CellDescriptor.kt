package org.ton.sdk.cell

import kotlin.jvm.JvmStatic

public class CellDescriptor(
    /**
     * First descriptor byte with a generic info about cell.
     */
    public val d1: Byte,

    /**
     * Second descriptor byte with a packed data size.
     */
    public val d2: Byte
) {
    public constructor(
        levelMask: LevelMask,
        isExotic: Boolean,
        referenceCount: Int,
        bitLength: Int
    ) : this(
        computeD1(levelMask, isExotic, referenceCount),
        computeD2(bitLength)
    )

    /**
     * [LevelMask] encoded in [d1].
     */
    public val levelMask: LevelMask get() = LevelMask((d1.toInt() and 0xFF) ushr 5)

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
     * Returns whether cell data is 8-bit aligned.
     */
    public val isAligned: Boolean get() = d2.toInt() and 1 == 0

    /**
     * Indicates whether the cell is absent.
     *
     * The property evaluates to `true` if the condition of the cell
     * being absent is fulfilled, based on specific bitmask comparison
     * against the value converted from `d1`. Otherwise, it evaluates
     * to `false`.
     */
    public val isAbsent: Boolean
        get() =
            d1.toInt() == (IS_EXOTIC_MASK or REFERENCE_COUNT_MASK)

    /**
     * Represents the byte length of the cell's data.
     *
     * This value is derived from the second descriptor byte (`d2`) of a `CellDescriptor`.
     * The method interprets the lower bit of `d2` as a flag indicating alignment and
     * extracts the remaining bits to compute the total byte length.
     *
     * The calculation involves adding the value of the least significant bit of `d2`
     * to half of the remaining bits of `d2`, resulting in the total byte length.
     *
     * By design, the value reflects how data is stored or packed in a cell, taking into
     * account possible alignment adjustments defined by the cell descriptor.
     */
    public val byteLength: Int
        get() {
            val d2 = d2.toInt() and 0xFF
            return (d2 and 1) + (d2 ushr 1)
        }

    /**
     * Retrieves the type of the cell represented by this descriptor.
     *
     * The `cellType` property determines the high-level classification of the cell based on
     * specific properties encoded in its metadata. This includes types such as:
     *
     * - `ORDINARY`: Represents a standard cell containing data and/or references.
     * - `PRUNED_BRANCH`: Represents a cell that has been pruned during a Merkle proof or update process.
     * - `LIBRARY_REFERENCE`: Indicates that the cell refers to a library cell and may be replaced by the referenced cell.
     * - `MERKLE_PROOF`: Represents a single-cell proof in a Merkle tree, primarily used for validation.
     * - `MERKLE_UPDATE`: Represents an update operation involving two cells in a Merkle tree.
     *
     * The type is determined by evaluating specific bitmasks and flag combinations defined in the descriptor's metadata.
     */
    public val cellType: CellType
        get() = d1.toInt().let { d1 ->
            if (d1 and IS_EXOTIC_MASK == 0) {
                CellType.ORDINARY
            } else when (d1 and REFERENCE_COUNT_MASK) {
                0 -> if (d1 and LEVEL_MASK == 0) {
                    CellType.LIBRARY_REFERENCE
                } else {
                    CellType.PRUNED_BRANCH
                }

                1 -> CellType.MERKLE_PROOF
                else -> CellType.MERKLE_UPDATE
            }
        }

    /**
     * Represents the count of hash values associated with the cell.
     *
     * For cells of type `PRUNED_BRANCH`, this always returns `1` as
     * a pruned branch cell contains exactly one hash value.
     * For other cell types, the count is determined based on the value of `levelMask`.
     */
    public val hashCount: Int
        get() {
            val levelMask = levelMask
            return if (cellType == CellType.PRUNED_BRANCH) {
                1
            } else {
                levelMask.hashCount
            }
        }

    /**
     * Creates a new descriptor with a new mask, shifted by the offset.
     */
    public fun virtualize(offset: Int): CellDescriptor {
        val virtualizedMask = ((d1.toInt() and 0xFF) ushr offset) and LEVEL_MASK
        val newD1 = virtualizedMask or (d1.toInt() and LEVEL_MASK.inv())
        return CellDescriptor(newD1.toByte(), d2)
    }

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
            return (bitLength / 8 + (bitLength + 7) / 8).toByte()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as CellDescriptor
        if (d1 != other.d1) return false
        if (d2 != other.d2) return false
        return true
    }

    override fun hashCode(): Int = (d1.toInt() shl 8) or d2.toInt()

    override fun toString(): String = "CellDescriptor(d1=0x${d1.toHexString()}, d2=0x${d2.toHexString()})"
}

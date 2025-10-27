package org.ton.sdk.blockchain.account

import kotlin.jvm.JvmField
import kotlin.jvm.JvmName

private const val UINT56_MAX_VALUE = (1L shl 56) - 1

/**
 * Represents storage usage details with the number of unique cells and total bits in those cells.
 *
 * This class includes validation to ensure that `cells` and `bits` are within the valid range [0..UINT56_MAX_VALUE].
 * It provides a concise representation of storage metrics used in various phases of processing.
 *
 * @property cells The number of unique cells used.
 * @property bits The total number of bits used in the unique cells.
 */
public class StorageUsedShort(
    @get:JvmName("cells") public val cells: Long,
    @get:JvmName("bits") public val bits: Long
) {
    init {
        require(cells in 0..UINT56_MAX_VALUE) { "Expected cells in range [0..$UINT56_MAX_VALUE], but was $cells" }
        require(bits in 0..UINT56_MAX_VALUE) { "Expected bits in range [0..$UINT56_MAX_VALUE], but was $bits" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StorageUsedShort) return false
        if (bits != other.bits) return false
        if (cells != other.cells) return false
        return true
    }

    override fun hashCode(): Int {
        var result = cells.hashCode()
        result = 31 * result + bits.hashCode()
        return result
    }

    override fun toString(): String = "StorageUsedShort(cells=$cells, bits=$bits)"

    public companion object {
        @JvmField
        public val ZERO: StorageUsedShort = StorageUsedShort(0, 0)
    }
}

package org.ton.sdk.blockchain.account

private const val UINT56_MAX_VALUE = (1L shl 56) - 1

/**
 * Amount of unique cells and bits.
 */
public class StorageUsedShort(
    /**
     * Amount of unique cells.
     */
    public val cells: Long,

    /**
     * The total number of bits in unique cells.
     */
    public val bits: Long
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
        public val ZERO: StorageUsedShort = StorageUsedShort(0, 0)
    }
}

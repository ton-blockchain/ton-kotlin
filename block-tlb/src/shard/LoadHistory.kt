@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.shard

public data class LoadHistory(
    public val mask: Long
) {
    public fun weight(): Int =
        ((mask and 0xffffL).countOneBits() * 3 +
            (mask and 0xffff_0000).countOneBits() * 2 +
            (mask and 0xffff_0000_0000).countOneBits()) - (3 + 2 + 1) * 16 * 2 / 3

    public fun addLoad(value: Boolean): LoadHistory {
        return if (value) {
            LoadHistory((mask shl 1) or 1)
        } else {
            LoadHistory(mask shl 1)
        }
    }

    public operator fun plus(value: Boolean): LoadHistory = addLoad(value)

    override fun toString(): String = "LoadHistory(${mask.toULong().toString(2).padStart(64, '0')})"
}

package org.ton.kotlin.blockchain.message.address

import org.ton.bitstring.BitString

/**
 * Anycast prefix info.
 *
 * ```tlb
 * anycast_info$_ depth:(#<= 30) { depth >= 1 } rewrite_pfx:(bits depth) = Anycast;
 * ```
 */
public class Anycast(
    public val depth: SplitDepth,
    public val rewritePrefix: BitString
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as Anycast
        if (depth != other.depth) return false
        if (rewritePrefix != other.rewritePrefix) return false
        return true
    }

    override fun hashCode(): Int {
        var result = depth.hashCode()
        result = 31 * result + rewritePrefix.hashCode()
        return result
    }

    override fun toString(): String = "Anycast(depth=$depth, rewritePrefix=$rewritePrefix)"
}

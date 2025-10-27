package org.ton.sdk.blockchain.address

import org.ton.sdk.bitstring.BitString
import kotlin.jvm.JvmName

/**
 * Anycast prefix info.
 *
 * ```tlb
 * anycast_info$_ depth:(#<= 30) { depth >= 1 } rewrite_pfx:(bits depth) = Anycast;
 * ```
 *
 * @property rewritePrefix Rewrite prefix bits (1..30). Routing may replace the first `depth` bits
 * of the destination account id with this prefix (see TON whitepaper §2.1, anycast routing).
 */
public class Anycast(
    @get:JvmName("rewritePrefix")
    public val rewritePrefix: BitString
) {
    init {
        require(rewritePrefix.size in 1..30) { "Rewrite prefix size must be between 1 and 30 bits, but was: ${rewritePrefix.size}" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Anycast

        return rewritePrefix == other.rewritePrefix
    }

    override fun hashCode(): Int = rewritePrefix.hashCode()

    override fun toString(): String = "Anycast(rewritePrefix=$rewritePrefix)"
}

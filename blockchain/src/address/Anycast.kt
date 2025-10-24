package org.ton.sdk.blockchain.address

import org.ton.bitstring.BitString

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
    public val rewritePrefix: BitString
) {
    init {
        require(rewritePrefix.size in 1..30) { "Rewrite prefix size must be between 1 and 30 bits, but was: ${rewritePrefix.size}" }
    }
}

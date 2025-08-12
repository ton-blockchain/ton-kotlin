package org.ton.kotlin.dht

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import org.ton.kotlin.adnl.AdnlIdFull
import org.ton.kotlin.adnl.AdnlIdShort
import org.ton.kotlin.dht.bucket.Distance
import org.ton.kotlin.dht.bucket.Key
import kotlin.experimental.xor

data class DhtKeyId(
    override val hash: ByteString
) : Key {
    constructor(adnlIdShort: AdnlIdShort) : this(
        hash = adnlIdShort.hash
    )

    constructor(adnlIdFull: AdnlIdFull) : this(
        hash = adnlIdFull.shortId.hash
    )

    override fun toString(): String = "DhtKeyId[${hash.toHexString()}]"

    /**
     * Returns the uniquely determined key with the given distance.
     *
     * This implements the following equivalence:
     *
     * `this xor other = distance <==> other = this xor distance`
     */
    fun forDistance(distance: Distance): DhtKeyId {
        return DhtKeyId(
            ByteString(*ByteArray(32) {
                distance.value[it] xor hash[it]
            })
        )
    }
}

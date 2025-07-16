package org.ton.kotlin.adnl

import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.crypto.PublicKey

data class AdnlIdShort(
    val hash: ByteString
) : Comparable<AdnlIdShort> {
    constructor(publicKey: PublicKey) : this(
        hash = publicKey.computeShortId()
    )

    override fun compareTo(other: AdnlIdShort): Int {
        return hash.compareTo(other.hash)
    }
}

data class AdnlIdFull(
    val publicKey: PublicKey,
) {
    val idShort: AdnlIdShort by lazy {
        AdnlIdShort(publicKey)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AdnlIdFull) return false

        if (publicKey != other.publicKey) return false

        return true
    }

    override fun hashCode(): Int {
        return publicKey.hashCode()
    }
}

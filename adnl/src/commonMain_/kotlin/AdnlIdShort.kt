package org.ton.kotlin.adnl

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import org.ton.kotlin.crypto.PublicKey

class AdnlIdShort(
    val hash: ByteString
) : Comparable<AdnlIdShort> {
    init {
        require(hash.size == 32) { "hash must be 32 bytes long" }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String = "AdnlIdShort[${hash.toHexString()}]"

    override fun compareTo(other: AdnlIdShort): Int {
        if (this === other) return 0
        for (i in 0 until 32) {
            val cmp = hash[i].toUByte().compareTo(other.hash[i].toUByte())
            if (cmp != 0) return cmp
        }
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AdnlIdShort

        return hash == other.hash
    }

    override fun hashCode(): Int = hash.hashCode()
}

class AdnlIdFull(
    val publicKey: PublicKey
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AdnlIdFull

        return publicKey == other.publicKey
    }

    override fun hashCode(): Int {
        return publicKey.hashCode()
    }

    override fun toString(): String = "AdnlIdFull(publicKey=$publicKey)"
}

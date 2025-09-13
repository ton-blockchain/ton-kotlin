@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.kotlin.overlay

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.crypto.PublicKey
import org.ton.kotlin.crypto.PublicKeyOverlay
import org.ton.kotlin.tl.Bits256
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
class OverlayIdShort(
    @Bits256
    val publicKeyHash: ByteString
) : Comparable<OverlayIdShort> {
    constructor(publicKey: PublicKey) : this(publicKey.computeShortId())
    constructor(idFull: OverlayIdFull) : this(idFull.publicKey.computeShortId())

    init {
        require(publicKeyHash.size == 32) { "Invalid public key hash size: ${publicKeyHash.size}, expected 32 bytes" }
    }

    override fun compareTo(other: OverlayIdShort): Int = publicKeyHash.compareTo(other.publicKeyHash)

    override fun toString(): String = publicKeyHash.toHexString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OverlayIdShort) return false
        return publicKeyHash == other.publicKeyHash
    }

    override fun hashCode(): Int = publicKeyHash.hashCode()
}

data class OverlayIdFull(
    val name: ByteString
) {
    val shortId by lazy {
        OverlayIdShort(this)
    }

    val publicKey get() = PublicKeyOverlay(name)

    fun shortId(): OverlayIdShort = shortId

    override fun toString(): String = "OverlayIdFull($name)"

    override fun hashCode(): Int = shortId.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OverlayIdFull) return false
        return shortId() == other.shortId()
    }
}

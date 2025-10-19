@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.kotlin.adnl

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.kotlin.crypto.PrivateKey
import org.ton.kotlin.crypto.PublicKey
import org.ton.kotlin.tl.TlFixedSize
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
public class AdnlIdShort(
    @TlFixedSize(32)
    public val hash: ByteString
) : Comparable<AdnlIdShort> {
    public constructor(publicKey: PublicKey) : this(
        hash = publicKey.computeShortId()
    )

    init {
        require(hash.size == 32) {
            "AdnlIdShort hash must be 32 bytes long, but was ${hash.size} bytes"
        }
    }

    override fun compareTo(other: AdnlIdShort): Int {
        return hash.compareTo(other.hash)
    }

    override fun toString(): String = hash.toHexString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AdnlIdShort) return false
        if (hash != other.hash) return false
        return true
    }

    override fun hashCode(): Int = hash.hashCode()
}

@Serializable(AdnlIdFull.Serializer::class)
public class AdnlIdFull(
    public val publicKey: PublicKey,
) {
    public constructor(privateKey: PrivateKey) : this(privateKey.publicKey())

    public val shortId: AdnlIdShort by lazy {
        AdnlIdShort(publicKey)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AdnlIdFull) return false
        if (publicKey != other.publicKey) return false
        return true
    }

    override fun hashCode(): Int = publicKey.hashCode()

    private object Serializer : kotlinx.serialization.KSerializer<AdnlIdFull> {
        private val delegate = PublicKey.serializer()
        override val descriptor = SerialDescriptor("AdnlIdFull", delegate.descriptor)

        override fun deserialize(decoder: Decoder): AdnlIdFull {
            return AdnlIdFull(decoder.decodeSerializableValue(delegate))
        }

        override fun serialize(encoder: Encoder, value: AdnlIdFull) {
            encoder.encodeSerializableValue(delegate, value.publicKey)
        }
    }
}

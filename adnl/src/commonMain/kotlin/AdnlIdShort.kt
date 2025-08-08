@file:UseSerializers(ByteStringSerializer::class)

package org.ton.kotlin.adnl

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.kotlin.adnl.serializers.ByteStringSerializer
import org.ton.kotlin.crypto.PrivateKey
import org.ton.kotlin.crypto.PublicKey
import org.ton.kotlin.tl.TlFixedSize

@Serializable
data class AdnlIdShort(
    @TlFixedSize(32)
    val hash: ByteString
) : Comparable<AdnlIdShort> {
    constructor(publicKey: PublicKey) : this(
        hash = publicKey.computeShortId()
    )

    override fun compareTo(other: AdnlIdShort): Int {
        return hash.compareTo(other.hash)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String = "AdnlIdShort[${hash.toHexString()}]"
}

@Serializable(AdnlIdFull.Serializer::class)
data class AdnlIdFull(
    val publicKey: PublicKey,
) {
    constructor(privateKey: PrivateKey) : this(privateKey.publicKey())

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

    object Serializer : kotlinx.serialization.KSerializer<AdnlIdFull> {
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

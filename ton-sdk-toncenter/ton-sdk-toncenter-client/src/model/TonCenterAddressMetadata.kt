package org.ton.sdk.toncenter.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.sdk.blockchain.address.AddressStd
import org.ton.sdk.toncenter.internal.serializers.AddressStdAsBase64Serializer
import kotlin.jvm.JvmName

@Serializable
public class TonCenterAddressMetadata(
    @get:JvmName("isIndexed")
    public val isIndexed: Boolean,
    @get:JvmName("tokenInfo")
    public val tokenInfo: List<TonCenterTokenInfo>
) {
    override fun toString(): String = "TonCenterAddressMetadata(isIndexed=$isIndexed, tokenInfo=$tokenInfo)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as TonCenterAddressMetadata
        if (isIndexed != other.isIndexed) return false
        if (tokenInfo != other.tokenInfo) return false
        return true
    }

    override fun hashCode(): Int {
        var result = isIndexed.hashCode()
        result = 31 * result + tokenInfo.hashCode()
        return result
    }
}

@Serializable(with = TonCenterMetadata.Serializer::class)
public class TonCenterMetadata(
    @get:JvmName("map")
    public val map: Map<AddressStd, TonCenterAddressMetadata>
) : Map<AddressStd, TonCenterAddressMetadata> by map {
    override fun toString(): String = "TonCenterMetadata($map)"

    private object Serializer : KSerializer<TonCenterMetadata> {
        private val serializer = MapSerializer(
            AddressStdAsBase64Serializer,
            TonCenterAddressMetadata.serializer()
        )
        override val descriptor: SerialDescriptor = SerialDescriptor("TonCenterMetadata", serializer.descriptor)

        override fun serialize(
            encoder: Encoder,
            value: TonCenterMetadata
        ) {
            serializer.serialize(encoder, value.map)
        }

        override fun deserialize(decoder: Decoder): TonCenterMetadata {
            return TonCenterMetadata(serializer.deserialize(decoder))
        }
    }
}

package org.ton.kotlin.provider.toncenter.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.kotlin.blockchain.message.address.AddressStd
import org.ton.kotlin.provider.toncenter.internal.serializers.AddressStdAsBase64Serializer

@Serializable
public data class TonCenterAddressMetadata(
    val isIndexed: Boolean,
    val tokenInfo: List<TonCenterTokenInfo>
)

@Serializable(with = TonCenterMetadata.Serializer::class)
public data class TonCenterMetadata(
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

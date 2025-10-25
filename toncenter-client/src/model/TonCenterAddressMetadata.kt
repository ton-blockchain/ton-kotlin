package org.ton.sdk.toncenter.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.sdk.blockchain.address.AddressStd
import org.ton.sdk.toncenter.internal.serializers.AddressStdAsBase64Serializer

@Serializable
public class TonCenterAddressMetadata(
    public val isIndexed: Boolean,
    public val tokenInfo: List<TonCenterTokenInfo>
)

@Serializable(with = TonCenterMetadata.Serializer::class)
public class TonCenterMetadata(
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

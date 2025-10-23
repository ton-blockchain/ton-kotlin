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
public data class TonCenterAddressBookRow(
    val userFriendly: String,
    val domain: String?
)

@Serializable(with = TonCenterAddressBook.Serializer::class)
public data class TonCenterAddressBook(
    public val map: Map<AddressStd, TonCenterAddressBookRow>
) : Map<AddressStd, TonCenterAddressBookRow> by map {
    override fun toString(): String = "TonCenterAddressBook($map)"

    private object Serializer : KSerializer<TonCenterAddressBook> {
        val serializer = MapSerializer(AddressStdAsBase64Serializer, TonCenterAddressBookRow.serializer())

        override val descriptor: SerialDescriptor = SerialDescriptor(
            "TonCenterAddressBook",
            serializer.descriptor
        )

        override fun serialize(
            encoder: Encoder,
            value: TonCenterAddressBook
        ) {
            serializer.serialize(encoder, value.map)
        }

        override fun deserialize(decoder: Decoder): TonCenterAddressBook {
            return TonCenterAddressBook(serializer.deserialize(decoder))
        }
    }
}

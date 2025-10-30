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
public class TonCenterAddressBookRow(
    @get:JvmName("userFriendly") public val userFriendly: String,
    @get:JvmName("domain") public val domain: String?
)

@Serializable(with = TonCenterAddressBook.Serializer::class)
public class TonCenterAddressBook(
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

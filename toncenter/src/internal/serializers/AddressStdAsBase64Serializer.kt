package org.ton.sdk.toncenter.internal.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.sdk.blockchain.address.AddressStd
import kotlin.io.encoding.Base64

public object AddressStdAsBase64Serializer : KSerializer<AddressStd> {
    private val serializer = String.serializer()

    override val descriptor: SerialDescriptor = SerialDescriptor("AddressStdAsBase64Serializer", serializer.descriptor)

    override fun serialize(
        encoder: Encoder,
        value: AddressStd
    ) {
        val string = value.toBase64String(format = Base64.UrlSafe, bounceable = false, testnet = false)
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): AddressStd {
        val string = decoder.decodeString()
        return AddressStd.parse(string)
    }
}

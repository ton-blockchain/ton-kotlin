package org.ton.sdk.toncenter.internal.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object IntHexSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        IntHexSerializer::class.toString(),
        PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: Int) {
        encoder.encodeString("0x${value.toHexString()}")
    }

    override fun deserialize(decoder: Decoder): Int {
        val string = decoder.decodeString()
        if (string.startsWith("0x")) {
            return string.substring(2).hexToInt()
        }
        return string.hexToInt()
    }
}

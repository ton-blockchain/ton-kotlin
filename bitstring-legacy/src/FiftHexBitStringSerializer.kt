package org.ton.bitstring

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public object FiftHexBitStringSerializer : KSerializer<BitString> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BitString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BitString {
        val hex = decoder.decodeString()
        return BitString(hex)
    }

    override fun serialize(encoder: Encoder, value: BitString) {
        encoder.encodeString(value.toString())
    }
}

package org.ton.sdk.tl.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlin.io.encoding.Base64

public object ByteArrayBase64Serializer : KSerializer<ByteArray> {
    private val byteArraySerializer = ByteArraySerializer()

    override val descriptor: SerialDescriptor = byteArraySerializer.descriptor

    override fun serialize(encoder: Encoder, value: ByteArray) {
        if (encoder is JsonEncoder) {
            encoder.encodeString(Base64.encode(value))
        } else {
            encoder.encodeSerializableValue(byteArraySerializer, value)
        }
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        return if (decoder is JsonDecoder) {
            Base64.decode(decoder.decodeString())
        } else {
            decoder.decodeSerializableValue(byteArraySerializer)
        }
    }
}

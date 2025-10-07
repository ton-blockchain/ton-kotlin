package org.ton.kotlin.tl.serializers

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.decodeToByteString
import kotlinx.io.bytestring.encode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlin.io.encoding.Base64

public object ByteStringBase64Serializer : KSerializer<ByteString> {
    private val byteArraySerializer = ByteArraySerializer()
    override val descriptor: SerialDescriptor =
        SerialDescriptor("ByteString", byteArraySerializer.descriptor)

    override fun serialize(encoder: Encoder, value: ByteString) {
        if (encoder is JsonEncoder) {
            encoder.encodeString(Base64.encode(value))
        } else {
            encoder.encodeSerializableValue(byteArraySerializer, value.toByteArray())
        }
    }

    override fun deserialize(decoder: Decoder): ByteString {
        return if (decoder is JsonDecoder) {
            return Base64.decodeToByteString(decoder.decodeString())
        } else {
            ByteString(*decoder.decodeSerializableValue(byteArraySerializer))
        }
    }
}

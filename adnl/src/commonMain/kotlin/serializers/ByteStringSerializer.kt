package org.ton.kotlin.adnl.serializers

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ByteStringSerializer : KSerializer<ByteString> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = SerialDescriptor("ByteString", ByteArraySerializer().descriptor)

    override fun serialize(encoder: Encoder, value: ByteString) {
        encoder.encodeSerializableValue(ByteArraySerializer(), value.toByteArray())
    }

    override fun deserialize(decoder: Decoder): ByteString {
        val byteArray = decoder.decodeSerializableValue(ByteArraySerializer())
        return ByteString(*byteArray)
    }
}

package org.ton.kotlin.crypto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalStdlibApi::class)
public object HexByteArraySerializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor = serialDescriptor<ByteArray>()

    override fun deserialize(decoder: Decoder): ByteArray = decoder.decodeString().hexToByteArray()

    override fun serialize(encoder: Encoder, value: ByteArray) {
        encoder.encodeString(value.toHexString())
    }
}

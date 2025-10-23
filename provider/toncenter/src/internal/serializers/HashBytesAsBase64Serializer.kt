package org.ton.kotlin.provider.toncenter.internal.serializers

import kotlinx.io.bytestring.decodeToByteString
import kotlinx.io.bytestring.encode
import kotlinx.io.bytestring.hexToByteString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.kotlin.crypto.HashBytes
import kotlin.io.encoding.Base64

internal object HashBytesAsBase64Serializer : KSerializer<HashBytes> {
    private val serializer = String.serializer()

    override val descriptor: SerialDescriptor = SerialDescriptor("HashBytesAsBase64Serializer", serializer.descriptor)

    override fun serialize(encoder: Encoder, value: HashBytes) {
        val string = Base64.UrlSafe.encode(value.value)
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): HashBytes {
        val string = decoder.decodeString()
        when (string.length) {
            64 -> return HashBytes(string.hexToByteString())
            66 -> if (string.startsWith("0x")) {
                return HashBytes(string.substring(2).hexToByteString())
            }

            44 -> {
                val format = if (string.indexOfAny(charArrayOf('_', '-')) != -1) {
                    Base64.UrlSafe
                } else {
                    Base64.Default
                }
                return HashBytes(format.decodeToByteString(string))
            }
        }
        throw IllegalArgumentException("Invalid hash bytes string")
    }
}

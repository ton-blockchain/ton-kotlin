package org.ton.kotlin.crypto.encoding

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.io.encoding.Base64

@Deprecated("Use kotlin stdlib instead", replaceWith = ReplaceWith("kotlin.io.encoding.Base64.decode(string)"))
public fun base64(string: String): ByteArray = Base64.decode(string)

@Deprecated("Use kotlin stdlib instead", replaceWith = ReplaceWith("kotlin.io.encoding.Base64.encode(byteArray)"))
public fun base64(byteArray: ByteArray): String = Base64.encode(byteArray)

@Deprecated("Use kotlin stdlib instead", replaceWith = ReplaceWith("kotlin.io.encoding.Base64.UrlSafe.decode(string)"))
public fun base64url(string: String): ByteArray = Base64.UrlSafe.decode(string)

@Deprecated(
    "Use kotlin stdlib instead",
    replaceWith = ReplaceWith("kotlin.io.encoding.Base64.UrlSafe.encode(byteArray)")
)
public fun base64url(byteArray: ByteArray): String = Base64.UrlSafe.encode(byteArray)

public object Base64ByteArraySerializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor = serialDescriptor<ByteArray>()

    override fun deserialize(decoder: Decoder): ByteArray = Base64.decode(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: ByteArray) {
        encoder.encodeString(Base64.encode(value))
    }
}

package org.ton.api.pk

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.api.pub.PublicKeyAes
import org.ton.kotlin.crypto.Decryptor
import org.ton.kotlin.crypto.DecryptorAes
import org.ton.kotlin.tl.ByteStringBase64Serializer
import org.ton.kotlin.tl.TlConstructor
import org.ton.kotlin.tl.TlReader
import org.ton.kotlin.tl.TlWriter

@JsonClassDiscriminator("@type")
@SerialName("pk.aes")
@Serializable
public data class PrivateKeyAes(
    @Serializable(ByteStringBase64Serializer::class)
    val key: ByteString
) : PrivateKey, Decryptor by DecryptorAes(key.toByteArray()) {
    override fun publicKey(): PublicKeyAes = PublicKeyAes(key)

    override fun toString(): String = toAdnlIdShort().toString()

    public companion object : TlConstructor<PrivateKeyAes>(
        schema = "pk.aes key:int256 = PrivateKey"
    ) {
        override fun encode(output: TlWriter, value: PrivateKeyAes) {
            output.writeRaw(value.key)
        }

        override fun decode(input: TlReader): PrivateKeyAes {
            val key = input.readByteString(32)
            return PrivateKeyAes(key)
        }
    }
}

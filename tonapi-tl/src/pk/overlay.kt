package org.ton.kotlin.api.pk

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.api.pub.PublicKeyOverlay
import org.ton.kotlin.crypto.Decryptor
import org.ton.kotlin.crypto.DecryptorFail
import org.ton.kotlin.tl.ByteStringBase64Serializer
import org.ton.kotlin.tl.TlConstructor
import org.ton.kotlin.tl.TlReader
import org.ton.kotlin.tl.TlWriter

@JsonClassDiscriminator("@type")
@SerialName("pk.overlay")
@Serializable
public data class PrivateKeyOverlay(
    @Serializable(ByteStringBase64Serializer::class)
    val name: ByteString
) : PrivateKey, Decryptor by DecryptorFail {
    override fun publicKey(): PublicKeyOverlay = PublicKeyOverlay(name)

    override fun toString(): String = toAdnlIdShort().toString()

    public companion object : TlConstructor<PrivateKeyOverlay>(
        schema = "pk.overlay name:bytes = PrivateKey"
    ) {
        override fun encode(writer: TlWriter, value: PrivateKeyOverlay) {
            writer.writeBytes(value.name)
        }

        override fun decode(reader: TlReader): PrivateKeyOverlay {
            val name = reader.readByteString()
            return PrivateKeyOverlay(name)
        }
    }
}

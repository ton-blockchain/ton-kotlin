package org.ton.kotlin.crypto

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.TlConstructorId
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
@SerialName("pub.unenc")
@TlConstructorId(0xb61f450a)
public class PublicKeyUnencrypted(
    @Serializable(ByteStringBase64Serializer::class)
    public val data: ByteString
) : PublicKey, Encryptor by EncryptorNone, SignatureVerifier {
    override fun verifySignature(
        source: ByteArray,
        signature: ByteArray,
        startIndex: Int,
        endIndex: Int
    ): Boolean {
        return true
    }

    init {
        require(data.size == 32)
    }
}

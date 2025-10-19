package org.ton.kotlin.crypto

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.TlConstructorId
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
@SerialName("pub.overlay")
@TlConstructorId(0x34ba45cb)
public data class PublicKeyOverlay(
    @Serializable(ByteStringBase64Serializer::class)
    val name: ByteString
) : PublicKey {
    override fun verifySignature(
        source: ByteArray,
        signature: ByteArray,
        startIndex: Int,
        endIndex: Int
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun encryptToByteArray(
        source: ByteArray,
        startIndex: Int,
        endIndex: Int
    ): ByteArray {
        TODO("Not yet implemented")
    }

    override fun encryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ) {
        TODO("Not yet implemented")
    }
}

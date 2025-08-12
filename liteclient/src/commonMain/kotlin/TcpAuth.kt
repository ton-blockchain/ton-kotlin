package org.ton.kotlin.liteclient

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import org.ton.kotlin.crypto.PublicKey
import org.ton.kotlin.tl.TlConstructorId
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
@TlConstructorId(0x445bab12)
data class TcpAuthentificate(
    @Serializable(ByteStringBase64Serializer::class)
    val nonce: ByteString
)

@Serializable
@TlConstructorId(0xe35d4ab6)
data class TcpAuthentificationNonce(
    @Serializable(ByteStringBase64Serializer::class)
    val nonce: ByteString
)

@Serializable
@TlConstructorId(0xf7ad9ea6)
data class TcpAuthentificationComplete(
    val key: PublicKey,
    @Serializable(ByteStringBase64Serializer::class)
    val signature: ByteString
)

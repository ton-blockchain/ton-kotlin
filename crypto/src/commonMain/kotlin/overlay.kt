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
) : PublicKey

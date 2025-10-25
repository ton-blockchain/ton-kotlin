package org.ton.sdk.crypto

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.sdk.tl.TlConstructorId
import org.ton.sdk.tl.serializers.ByteStringBase64Serializer

@Serializable
@SerialName("pub.overlay")
@TlConstructorId(0x34ba45cb)
public data class PublicKeyOverlay(
    @Serializable(ByteStringBase64Serializer::class)
    val name: ByteString
) : PublicKey

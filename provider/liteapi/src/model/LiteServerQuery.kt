package org.ton.sdk.provider.liteapi.model

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import org.ton.sdk.tl.TlConstructorId
import org.ton.sdk.tl.serializers.ByteStringBase64Serializer

@Serializable
@TlConstructorId(0x798c06df)
public class LiteServerQuery(
    @Serializable(ByteStringBase64Serializer::class)
    public val data: ByteString
)

package org.ton.kotlin.provider.liteapi.model

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.TlConstructorId
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
@TlConstructorId(0x798c06df)
public class LiteServerQuery(
    @Serializable(ByteStringBase64Serializer::class)
    public val data: ByteString
)

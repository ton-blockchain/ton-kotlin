package org.ton.lite.api.liteserver.functions

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.query")
public data class LiteServerQuery(
    @get:JvmName("data")
    @Serializable(ByteStringBase64Serializer::class)
    val data: ByteString
) {
    public companion object : TlCodec<LiteServerQuery> by LiteServerQueryTlConstructor
}

private object LiteServerQueryTlConstructor : TlConstructor<LiteServerQuery>(
    schema = "liteServer.query data:bytes = Object"
) {
    override fun decode(input: TlReader): LiteServerQuery {
        val data = input.readByteString()
        return LiteServerQuery(data)
    }

    override fun encode(output: TlWriter, value: LiteServerQuery) {
        output.writeBytes(value.data)
    }
}

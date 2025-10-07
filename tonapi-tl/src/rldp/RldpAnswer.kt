package org.ton.api.rldp

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.*

@SerialName("rldp.answer")
@Serializable
public data class RldpAnswer(
    @SerialName("query_id")
    @Serializable(ByteStringBase64Serializer::class)
    val queryId: ByteString,
    @Serializable(ByteStringBase64Serializer::class)
    override val data: ByteString
) : RldpMessage {
    override val id: ByteString
        get() = queryId

    override fun tlCodec(): TlCodec<RldpAnswer> = Companion

    public companion object : TlConstructor<RldpAnswer>(
        schema = "rldp.answer query_id:int256 data:bytes = rldp.Message",
    ) {
        override fun encode(writer: TlWriter, value: RldpAnswer) {
            writer.writeRaw(value.queryId)
            writer.writeBytes(value.data)
        }

        override fun decode(reader: TlReader): RldpAnswer {
            val queryId = reader.readByteString(32)
            val data = reader.readByteString()
            return RldpAnswer(queryId, data)
        }
    }
}

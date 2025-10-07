package org.ton.api.rldp

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.*

@Serializable
@SerialName("rldp.query")
public data class RldpQuery(
    @SerialName("query_id")
    @Serializable(ByteStringBase64Serializer::class)
    val queryId: ByteString,
    @SerialName("max_answer_size")
    val maxAnswerSize: Long,
    val timeout: Int,
    @Serializable(ByteStringBase64Serializer::class)
    override val data: ByteString
) : RldpMessage {
    override val id: ByteString
        get() = queryId

    override fun tlCodec(): TlCodec<RldpQuery> = Companion

    public companion object : TlConstructor<RldpQuery>(
        schema = "rldp.query query_id:int256 max_answer_size:long timeout:int data:bytes = rldp.Message",
    ) {
        override fun encode(writer: TlWriter, value: RldpQuery) {
            writer.writeRaw(value.queryId.toByteArray())
            writer.writeLong(value.maxAnswerSize)
            writer.writeInt(value.timeout)
            writer.writeBytes(value.data)
        }

        override fun decode(reader: TlReader): RldpQuery {
            val queryId = reader.readByteString(32)
            val maxAnswerSize = reader.readLong()
            val timeout = reader.readInt()
            val data = reader.readByteString()
            return RldpQuery(queryId, maxAnswerSize, timeout, data)
        }
    }
}

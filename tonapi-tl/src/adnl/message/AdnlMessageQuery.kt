package org.ton.api.adnl.message

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.ByteStringBase64Serializer
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter
import org.ton.tl.constructors.BytesTlConstructor
import kotlin.jvm.JvmName

@Serializable
@SerialName("adnl.message.query")
public data class AdnlMessageQuery(
    @SerialName("query_id")
    @get:JvmName("queryId")
    @Serializable(ByteStringBase64Serializer::class)
    val queryId: ByteString,

    @get:JvmName("query")
    @Serializable(ByteStringBase64Serializer::class)
    val query: ByteString
) : AdnlMessage {
    public companion object : TlConstructor<AdnlMessageQuery>(
        schema = "adnl.message.query query_id:int256 query:bytes = adnl.Message",
    ) {
        public fun sizeOf(value: AdnlMessageQuery): Int =
            256 / Byte.SIZE_BYTES + BytesTlConstructor.sizeOf(value.query)

        override fun encode(writer: TlWriter, value: AdnlMessageQuery) {
            writer.writeRaw(value.queryId)
            writer.writeBytes(value.query)
        }

        override fun decode(reader: TlReader): AdnlMessageQuery {
            val queryId = reader.readByteString(32)
            val query = reader.readByteString()
            return AdnlMessageQuery(queryId, query)
        }
    }
}

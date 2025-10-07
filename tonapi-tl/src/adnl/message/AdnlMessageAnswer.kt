package org.ton.api.adnl.message

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.*
import org.ton.tl.constructors.BytesTlConstructor
import kotlin.jvm.JvmName

@SerialName("adnl.message.answer")
@Serializable
public data class AdnlMessageAnswer(
    @SerialName("query_id")
    @get:JvmName("queryId")
    @Serializable(ByteStringBase64Serializer::class)
    val queryId: ByteString,

    @get:JvmName("answer")
    @Serializable(ByteStringBase64Serializer::class)
    val answer: ByteString
) : AdnlMessage {
    init {
        require(queryId.size == 32)
    }

    public companion object : TlConstructor<AdnlMessageAnswer>(
        schema = "adnl.message.answer query_id:int256 answer:bytes = adnl.Message",
    ) {
        public fun sizeOf(value: AdnlMessageAnswer): Int =
            256 / 8 + BytesTlConstructor.sizeOf(value.answer)

        override fun encode(writer: TlWriter, value: AdnlMessageAnswer): Unit = writer {
            writeRaw(value.queryId.toByteArray())
            writeBytes(value.answer)
        }

        override fun decode(reader: TlReader): AdnlMessageAnswer = reader {
            val queryId = readByteString(32)
            val answer = readByteString()
            AdnlMessageAnswer(queryId, answer)
        }
    }
}

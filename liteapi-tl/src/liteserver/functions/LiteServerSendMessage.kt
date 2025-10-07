package org.ton.lite.api.liteserver.functions

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.lite.api.liteserver.LiteServerSendMsgStatus
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.sendMessage")
public data class LiteServerSendMessage(
    @get:JvmName("body")
    @Serializable(ByteStringBase64Serializer::class)
    val body: ByteString
) : TLFunction<LiteServerSendMessage, LiteServerSendMsgStatus> {
    override fun tlCodec(): TlCodec<LiteServerSendMessage> = LiteServerSendMessageTlConstructor

    override fun resultTlCodec(): TlCodec<LiteServerSendMsgStatus> = LiteServerSendMsgStatus

    public companion object : TlCodec<LiteServerSendMessage> by LiteServerSendMessageTlConstructor
}

private object LiteServerSendMessageTlConstructor : TlConstructor<LiteServerSendMessage>(
    schema = "liteServer.sendMessage body:bytes = liteServer.SendMsgStatus"
) {
    override fun decode(reader: TlReader): LiteServerSendMessage {
        val body = reader.readByteString()
        return LiteServerSendMessage(body)
    }

    override fun encode(writer: TlWriter, value: LiteServerSendMessage) {
        writer.writeBytes(value.body)
    }
}

package org.ton.api.tcp

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.sdk.crypto.PublicKey
import org.ton.sdk.tl.TL
import org.ton.sdk.tl.serializers.ByteStringBase64Serializer
import org.ton.tl.TlCodec
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter

@SerialName("tcp.authentificationComplete")
@Serializable
public data class TcpAuthentificationComplete(
    val key: PublicKey,
    @Serializable(ByteStringBase64Serializer::class)
    val signature: ByteString
) : TcpMessage {
    public companion object : TlCodec<TcpAuthentificationComplete> by TcpAuthentificationCompleteTlConstructor
}

private object TcpAuthentificationCompleteTlConstructor : TlConstructor<TcpAuthentificationComplete>(
    schema = "tcp.authentificationComplete key:PublicKey signature:bytes = tcp.Message"
) {
    override fun decode(reader: TlReader): TcpAuthentificationComplete {
        val key = TL.Boxed.decodeFromSource(PublicKey.serializer(), reader.input)
        val signature = reader.readByteString()
        return TcpAuthentificationComplete(key, signature)
    }

    override fun encode(writer: TlWriter, value: TcpAuthentificationComplete) {
        TL.Boxed.encodeIntoSink(PublicKey.serializer(), value.key, writer.output)
        writer.writeBytes(value.signature)
    }
}

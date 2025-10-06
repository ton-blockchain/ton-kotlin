package org.ton.api.tcp

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.pub.PublicKey
import org.ton.kotlin.tl.*

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
        val key = reader.read(PublicKey)
        val signature = reader.readByteString()
        return TcpAuthentificationComplete(key, signature)
    }

    override fun encode(writer: TlWriter, value: TcpAuthentificationComplete) {
        writer.write(PublicKey, value.key)
        writer.writeBytes(value.signature)
    }
}

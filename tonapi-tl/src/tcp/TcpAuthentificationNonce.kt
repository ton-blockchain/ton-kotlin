package org.ton.api.tcp

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.*

@SerialName("tcp.authentificationNonce")
@Serializable
public data class TcpAuthentificationNonce(
    @Serializable(ByteStringBase64Serializer::class)
    val nonce: ByteString
) {
    public companion object : TlCodec<TcpAuthentificationNonce> by TcpAuthentificationNonceTlConstructor
}

private object TcpAuthentificationNonceTlConstructor : TlConstructor<TcpAuthentificationNonce>(
    schema = "tcp.authentificationNonce nonce:bytes = tcp.Message"
) {
    override fun decode(reader: TlReader): TcpAuthentificationNonce {
        val nonce = reader.readByteString()
        return TcpAuthentificationNonce(nonce)
    }

    override fun encode(writer: TlWriter, value: TcpAuthentificationNonce) {
        writer.writeBytes(value.nonce)
    }
}

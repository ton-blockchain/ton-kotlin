package org.ton.api.tcp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.TlCodec
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter

@SerialName("tcp.authentificate")
@Serializable
public data class TcpAuthentificate(
    val nonce: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TcpAuthentificate) return false
        if (!nonce.contentEquals(other.nonce)) return false
        return true
    }

    override fun hashCode(): Int = nonce.contentHashCode()

    public companion object : TlCodec<TcpAuthentificate> by TcpAuthentificateTlConstructor
}

private object TcpAuthentificateTlConstructor : TlConstructor<TcpAuthentificate>(
    schema = "tcp.authentificate nonce:bytes = tcp.Authentificate"
) {
    override fun decode(input: TlReader): TcpAuthentificate {
        val nonce = input.readBytes()
        return TcpAuthentificate(nonce)
    }

    override fun encode(output: TlWriter, value: TcpAuthentificate) {
        output.writeBytes(value.nonce)
    }
}

package org.ton.api.tcp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.TlCodec
import org.ton.kotlin.tl.TlConstructor
import org.ton.kotlin.tl.TlReader
import org.ton.kotlin.tl.TlWriter

@Serializable
@SerialName("tcp.pong")
public data class TcpPong(
    @SerialName("random_id")
    val randomId: Long
) {
    public companion object : TlCodec<TcpPong> by TcpPongTlConstructor
}

private object TcpPongTlConstructor : TlConstructor<TcpPong>(
    schema = "tcp.pong random_id:long = tcp.Pong"
) {
    override fun decode(reader: TlReader): TcpPong {
        val randomId = reader.readLong()
        return TcpPong(randomId)
    }

    override fun encode(writer: TlWriter, value: TcpPong) {
        writer.writeLong(value.randomId)
    }
}

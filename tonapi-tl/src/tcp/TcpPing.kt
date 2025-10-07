package org.ton.api.tcp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.TlCodec
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter

@Serializable
@SerialName("tcp.ping")
public data class TcpPing(
    @SerialName("random_id")
    val randomId: Long
) {
    public companion object : TlCodec<TcpPing> by TcpPingTlConstructor
}

private object TcpPingTlConstructor : TlConstructor<TcpPing>(
    schema = "tcp.ping random_id:long = tcp.Pong"
) {
    override fun decode(input: TlReader): TcpPing {
        val randomId = input.readLong()
        return TcpPing(randomId)
    }

    override fun encode(output: TlWriter, value: TcpPing) {
        output.writeLong(value.randomId)
    }
}

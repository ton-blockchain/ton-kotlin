package org.ton.api.dht

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.*

@Serializable
public data class DhtPong(
    @SerialName("random_id")
    val randomId: Long
) : TlObject<DhtPong> {
    override fun tlCodec(): TlCodec<DhtPong> = DhtPongTlConstructor

    public companion object : TlCodec<DhtPong> by DhtPongTlConstructor
}

private object DhtPongTlConstructor : TlConstructor<DhtPong>(
    schema = "dht.pong random_id:long = dht.Pong"
) {
    override fun encode(writer: TlWriter, value: DhtPong) {
        writer.writeLong(value.randomId)
    }

    override fun decode(input: TlReader): DhtPong {
        val randomId = input.readLong()
        return DhtPong(randomId)
    }
}

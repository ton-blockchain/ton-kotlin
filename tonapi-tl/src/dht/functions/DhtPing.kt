package org.ton.api.dht.functions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.dht.DhtPong
import org.ton.tl.*

@SerialName("dht.ping")
@Serializable
public data class DhtPing(
    @SerialName("random_id")
    val randomId: Long
) : TLFunction<DhtPing, DhtPong> {
    override fun tlCodec(): TlCodec<DhtPing> = DhtPing
    override fun resultTlCodec(): TlCodec<DhtPong> = DhtPong

    public companion object : TlCodec<DhtPing> by DhtPingTlConstructor
}

private object DhtPingTlConstructor : TlConstructor<DhtPing>(
    schema = "dht.ping random_id:long = dht.Pong"
) {
    override fun decode(input: TlReader): DhtPing {
        val random_id = input.readLong()
        return DhtPing(random_id)
    }

    override fun encode(output: TlWriter, value: DhtPing) {
        output.writeLong(value.randomId)
    }
}

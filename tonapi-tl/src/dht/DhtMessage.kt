package org.ton.api.dht

import kotlinx.serialization.Serializable
import org.ton.tl.*

@Serializable
public data class DhtMessage(
    val node: DhtNode
) : TlObject<DhtMessage> {
    override fun tlCodec(): TlCodec<DhtMessage> = Companion

    public companion object : TlConstructor<DhtMessage>(
        schema = "dht.message node:dht.node = dht.Message"
    ) {
        override fun encode(writer: TlWriter, value: DhtMessage) {
            writer.write(DhtNode, value.node)
        }

        override fun decode(reader: TlReader): DhtMessage {
            val node = reader.read(DhtNode)
            return DhtMessage(node)
        }
    }
}

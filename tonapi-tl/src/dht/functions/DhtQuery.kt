package org.ton.kotlin.api.dht.functions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.api.dht.DhtNode
import org.ton.kotlin.tl.*

public interface DhtQueryFunction {
    public fun query(node: DhtNode)
}

@SerialName("dht.query")
@Serializable
public data class DhtQuery(
    val node: DhtNode
) {
    public companion object : TlCodec<DhtQuery> by DhtQueryTlConstructor
}

private object DhtQueryTlConstructor : TlConstructor<DhtQuery>(
    schema = "dht.query node:dht.node = True"
) {
    override fun decode(reader: TlReader): DhtQuery {
        val node = reader.read(DhtNode)
        return DhtQuery(node)
    }

    override fun encode(writer: TlWriter, value: DhtQuery) {
        writer.write(DhtNode, value.node)
    }
}

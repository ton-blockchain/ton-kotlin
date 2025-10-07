package org.ton.api.dht.db

import kotlinx.serialization.Serializable
import org.ton.api.dht.DhtNode
import org.ton.api.dht.DhtNodes
import org.ton.tl.*

@Serializable
public data class DhtDbBucket(
    val nodes: DhtNodes
) : TlObject<DhtDbBucket> {
    public constructor(nodes: List<DhtNode>) : this(DhtNodes(nodes))

    override fun tlCodec(): TlCodec<DhtDbBucket> = Companion

    public companion object : TlConstructor<DhtDbBucket>(
        schema = "dht.db.bucket nodes:dht.nodes = dht.db.Bucket"
    ) {
        override fun encode(writer: TlWriter, value: DhtDbBucket) {
            writer.write(DhtNodes, value.nodes)
        }

        override fun decode(reader: TlReader): DhtDbBucket {
            val nodes = reader.read(DhtNodes)
            return DhtDbBucket(nodes)
        }
    }
}

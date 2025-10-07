@file:Suppress("OPT_IN_USAGE")

package org.ton.api.dht

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.tl.*

@Serializable
@JsonClassDiscriminator("@type")
public sealed interface DhtValueResult : TlObject<DhtValueResult> {
    public fun valueOrNull(): DhtValue?

    override fun tlCodec(): TlCodec<out DhtValueResult> = Companion

    public companion object : TlCombinator<DhtValueResult>(
        DhtValueResult::class,
        DhtValueNotFound::class to DhtValueNotFound,
        DhtValueFound::class to DhtValueFound
    )
}

@Serializable
@SerialName("dht.valueNotFound")
public data class DhtValueNotFound(
    val nodes: DhtNodes
) : DhtValueResult {
    override fun valueOrNull(): DhtValue? = null

    public companion object : TlConstructor<DhtValueNotFound>(
        schema = "dht.valueNotFound nodes:dht.nodes = dht.ValueResult"
    ) {
        override fun encode(writer: TlWriter, value: DhtValueNotFound) {
            writer.write(DhtNodes, value.nodes)
        }

        override fun decode(reader: TlReader): DhtValueNotFound {
            val nodes = reader.read(DhtNodes)
            return DhtValueNotFound(nodes)
        }
    }
}

@Serializable
@SerialName("dht.valueFound")
public data class DhtValueFound(
    val value: DhtValue
) : DhtValueResult {
    override fun valueOrNull(): DhtValue = value

    public companion object : TlConstructor<DhtValueFound>(
        schema = "dht.valueFound value:dht.Value = dht.ValueResult"
    ) {
        override fun encode(writer: TlWriter, value: DhtValueFound) {
            DhtValue.encodeBoxed(writer, value.value)
        }

        override fun decode(reader: TlReader): DhtValueFound {
            val value = DhtValue.decodeBoxed(reader)
            return DhtValueFound(value)
        }
    }
}

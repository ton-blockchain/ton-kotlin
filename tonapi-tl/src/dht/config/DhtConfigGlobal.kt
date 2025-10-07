@file:Suppress("OPT_IN_USAGE", "PropertyName")

package org.ton.api.dht.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.api.dht.DhtNode
import org.ton.api.dht.DhtNodes
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("dht.config.global")
@JsonClassDiscriminator("@type")
public data class DhtConfigGlobal(
    @SerialName("static_nodes")
    @get:JvmName("staticNodes")
    val staticNodes: DhtNodes = DhtNodes(),

    @get:JvmName("k")
    val k: Int = 0,

    @get:JvmName("a")
    val a: Int = 0
) : TlObject<DhtConfigGlobal> {
    public constructor(
        staticNodes: List<DhtNode>,
        k: Int,
        a: Int
    ) : this(DhtNodes(staticNodes), k, a)

    override fun tlCodec(): TlCodec<DhtConfigGlobal> = Companion

    public companion object : TlConstructor<DhtConfigGlobal>(
        schema = "dht.config.global static_nodes:dht.nodes k:int a:int = dht.config.Global"
    ) {
        override fun encode(output: TlWriter, value: DhtConfigGlobal) {
            output.write(DhtNodes, value.staticNodes)
            output.writeInt(value.k)
            output.writeInt(value.a)
        }

        override fun decode(input: TlReader): DhtConfigGlobal {
            val staticNodes = input.read(DhtNodes)
            val k = input.readInt()
            val a = input.readInt()
            return DhtConfigGlobal(staticNodes, k, a)
        }
    }
}

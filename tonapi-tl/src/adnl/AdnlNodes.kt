package org.ton.api.adnl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("adnl.nodes")
public data class AdnlNodes(
    @get:JvmName("nodes")
    val nodes: List<AdnlNode> = emptyList()
) {
    public companion object : TlCodec<AdnlNodes> by AdnlNodesTlConstructor
}

private object AdnlNodesTlConstructor : TlConstructor<AdnlNodes>(
    schema = "adnl.nodes nodes:(vector adnl.node) = adnl.Nodes"
) {
    override fun encode(writer: TlWriter, value: AdnlNodes) = writer {
        writeVector(value.nodes) {
            write(AdnlNode, it)
        }
    }

    override fun decode(reader: TlReader): AdnlNodes = reader {
        val nodes = readVector {
            read(AdnlNode)
        }
        AdnlNodes(nodes)
    }
}

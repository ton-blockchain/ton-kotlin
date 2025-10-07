package org.ton.api.overlay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.*

@Serializable
@SerialName("overlay.nodes")
public class OverlayNodes(
    public val nodes: List<OverlayNode>
) : TlObject<OverlayNodes> {
    public constructor(vararg nodes: OverlayNode) : this(nodes.toList())

    override fun tlCodec(): TlCodec<OverlayNodes> = Companion

    public companion object : TlConstructor<OverlayNodes>(
        schema = "overlay.nodes nodes:(vector overlay.node) = overlay.Nodes",
    ) {
        override fun encode(output: TlWriter, value: OverlayNodes) {
            output.writeVector(value.nodes) {
                write(OverlayNode, it)
            }
        }

        override fun decode(input: TlReader): OverlayNodes {
            val nodes = input.readVector {
                read(OverlayNode)
            }
            return OverlayNodes(nodes)
        }
    }
}

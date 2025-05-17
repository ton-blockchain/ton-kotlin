@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.block

import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.TlbCombinator
import org.ton.kotlin.tlb.TlbObject
import kotlin.jvm.JvmStatic


@JsonClassDiscriminator("@type")
public sealed interface BinTree<X> : Iterable<X>, TlbObject {

    override fun iterator(): Iterator<X> = nodes().iterator()
    public fun nodes(): Sequence<X>

    public companion object {
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        public fun <X> tlbCodec(
            x: TlbCodec<X>
        ): TlbCombinator<BinTree<X>> = BinTreeTlbCombinator(x) as TlbCombinator<BinTree<X>>
    }
}

private class BinTreeTlbCombinator(
    x: TlbCodec<*>
) : TlbCombinator<BinTree<*>>(
    BinTree::class,
    BinTreeLeaf::class to BinTreeLeaf.tlbCodec(x),
    BinTreeFork::class to BinTreeFork.tlbCodec(x)
)

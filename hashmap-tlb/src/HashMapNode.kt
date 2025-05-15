@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.hashmap

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.TlbObject
import kotlin.jvm.JvmStatic

@Serializable
@JsonClassDiscriminator("@type")
public sealed interface HashMapNode<T> : TlbObject {
    public companion object {
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        public fun <X> tlbCodec(n: Int, x: TlbCodec<X>): TlbCodec<HashMapNode<X>> =
            if (n == 0) {
                HmnLeaf.tlbCodec(x)
            } else {
                HmnFork.tlbCodec(n, x)
            } as TlbCodec<HashMapNode<X>>

        public fun <T> leaf(value: T): HmnLeaf<T> = HmnLeaf(value)
    }
}

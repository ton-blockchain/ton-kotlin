package org.ton.kotlin.hashmap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*

@Serializable
@SerialName("hmn_leaf")
public data class HmnLeaf<T>(
    val value: T
) : HashMapNode<T> {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("hmn_leaf") {
            field("value", value)
        }
    }

    override fun toString(): String = print().toString()

    public companion object {
        public fun <T> tlbCodec(x: TlbCodec<T>): TlbCodec<HmnLeaf<T>> =
            HashMapNodeLeafTlbConstructor(x)
    }
}

private class HashMapNodeLeafTlbConstructor<X>(
    val x: TlbCodec<X>
) : TlbConstructor<HmnLeaf<X>>(
    schema = "hmn_leaf#_ {X:Type} value:X = HashmapNode 0 X;",
    id = BitString.empty()
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: HmnLeaf<X>
    ) = cellBuilder {
        storeTlb(x, value.value)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): HmnLeaf<X> = cellSlice {
        val value = loadTlb(x)
        HmnLeaf(value)
    }
}

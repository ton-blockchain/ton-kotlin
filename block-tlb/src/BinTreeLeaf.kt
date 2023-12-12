package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import kotlin.jvm.JvmStatic

@Serializable
@SerialName("bt_leaf")
public data class BinTreeLeaf<X>(
    val leaf: X
) : BinTree<X> {
    override fun nodes(): Sequence<X> = sequenceOf(leaf)

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("bt_leaf") {
            field("leaf", leaf)
        }
    }

    override fun toString(): String = print().toString()

    public companion object {
        @JvmStatic
        public fun <X> tlbCodec(
            x: TlbCodec<X>
        ): TlbConstructor<BinTreeLeaf<X>> = BinTreeTlbConstructor(x)

        public inline fun <reified X> invoke(x: TlbCodec<X>): TlbConstructor<BinTreeLeaf<X>> = tlbCodec(x)
    }
}

private class BinTreeTlbConstructor<X>(
    val x: TlbCodec<X>
) : TlbConstructor<BinTreeLeaf<X>>(
    schema = "bt_leaf\$0 {X:Type} leaf:X = BinTree X;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: BinTreeLeaf<X>
    ) = cellBuilder {
        storeTlb(x, value.leaf)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): BinTreeLeaf<X> = cellSlice {
        val leaf = loadTlb(x)
        BinTreeLeaf(leaf)
    }
}

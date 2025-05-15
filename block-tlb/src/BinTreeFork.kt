package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import kotlin.jvm.JvmStatic


@SerialName("bt_fork")
public data class BinTreeFork<X>(
    val left: CellRef<BinTree<X>>,
    val right: CellRef<BinTree<X>>
) : BinTree<X> {
    override fun nodes(): Sequence<X> = sequence {
        yieldAll(left.value.nodes())
        yieldAll(right.value.nodes())
    }

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("bt_fork") {
            field("left", left)
            field("right", right)
        }
    }

    override fun toString(): String = print().toString()

    public companion object {
        @JvmStatic
        public fun <X> tlbCodec(
            x: TlbCodec<X>
        ): TlbConstructor<BinTreeFork<X>> = BinTreeForkTlbConstructor(x)
    }
}

private class BinTreeForkTlbConstructor<X>(
    val x: TlbCodec<X>
) : TlbConstructor<BinTreeFork<X>>(
    schema = "bt_fork\$1 {X:Type} left:^(BinTree X) right:^(BinTree X) = BinTree X;"
) {
    val binTree by lazy(LazyThreadSafetyMode.PUBLICATION) {
        BinTree.tlbCodec(x)
    }

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: BinTreeFork<X>
    ) = cellBuilder {
        storeRef(binTree, value.left)
        storeRef(binTree, value.right)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): BinTreeFork<X> = cellSlice {
        val left = loadRef(binTree)
        val right = loadRef(binTree)
        BinTreeFork(left, right)
    }
}

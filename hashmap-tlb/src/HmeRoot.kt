package org.ton.hashmap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.bitstring.BitString
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.*
import kotlin.jvm.JvmStatic

@Serializable
@SerialName("hme_root")
public data class HmeRoot<T>(
    val root: CellRef<HmEdge<T>>
) : HashMapE<T> {
    public constructor(root: Cell, tlbCodec: TlbCodec<HmEdge<T>>) : this(CellRef(root, tlbCodec))
    public constructor(root: HmEdge<T>) : this(CellRef(root))

    override fun iterator(): Iterator<Pair<BitString, T>> = root.value.iterator()

    override fun set(key: BitString, value: T): HmeRoot<T> {
        val root = root.value.set(key, value)
        return HmeRoot(root)
    }

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("hme_root") {
            field("root", root)
        }
    }

    override fun toString(): String = print().toString()

    public companion object {
        @JvmStatic
        public fun <X> tlbConstructor(n: Int, x: TlbCodec<X>): TlbConstructor<HmeRoot<X>> =
            RootHashMapETlbConstructor(n, x)
    }
}

private class RootHashMapETlbConstructor<X>(
    n: Int,
    x: TlbCodec<X>
) : TlbConstructor<HmeRoot<X>>(
    schema = "hme_root\$1 {n:#} {X:Type} root:^(Hashmap n X) = HashmapE n X;",
    id = ID
) {
    private val cellRef = CellRef.tlbCodec(HmEdge.tlbCodec(n, x))

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: HmeRoot<X>
    ) {
        cellBuilder.storeTlb(cellRef, value.root)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): HmeRoot<X> {
        val root = cellSlice.loadTlb(cellRef)
        return HmeRoot(root)
    }

    companion object {
        val ID = BitString(true)
    }
}

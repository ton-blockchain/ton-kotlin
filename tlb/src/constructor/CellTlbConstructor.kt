package org.ton.tlb.constructor

import org.ton.bitstring.BitString
import org.ton.cell.*
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

public fun Cell.Companion.tlbCodec(): TlbCodec<Cell> = CellTlbConstructor
public fun <T : Any> Cell.Companion.tlbCodec(type: TlbCodec<T>): TlbCodec<T> = CellReferencedTlbConstructor(type)

private object CellTlbConstructor : TlbConstructor<Cell>(
    schema = "_ _:Cell = Cell;",
    id = BitString.empty()
) {
    override fun storeTlb(
        builder: CellBuilder, value: Cell
    ) = builder {
        storeRef(value)
    }

    override fun loadTlb(
        slice: CellSlice
    ): Cell = slice {
        loadRef()
    }
}

private class CellReferencedTlbConstructor<T : Any>(
    val codec: TlbCodec<T>
) : TlbConstructor<T>("", id = BitString.empty()) {
    override fun storeTlb(
        cellBuilder: CellBuilder, value: T
    ) = cellBuilder {
        storeRef {
            storeTlb(codec, value)
        }
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): T = cellSlice {
        loadRef {
            loadTlb(codec)
        }
    }
}

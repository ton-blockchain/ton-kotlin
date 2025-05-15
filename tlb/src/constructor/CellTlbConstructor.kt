package org.ton.kotlin.tlb.constructor

import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.*
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.storeTlb

public fun Cell.Companion.tlbCodec(): TlbCodec<Cell> = CellTlbConstructor
public fun <T : Any> Cell.Companion.tlbCodec(type: TlbCodec<T>): TlbCodec<T> = CellReferencedTlbConstructor(type)

private object CellTlbConstructor : TlbConstructor<Cell>(
    schema = "_ _:Cell = Cell;",
    id = BitString.empty()
) {
    override fun storeTlb(
        cellBuilder: CellBuilder, value: Cell
    ) = cellBuilder {
        storeRef(value)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): Cell = cellSlice {
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

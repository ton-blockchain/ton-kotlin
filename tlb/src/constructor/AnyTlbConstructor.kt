package org.ton.tlb.constructor

import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.buildCell
import org.ton.kotlin.cell.CellContext
import org.ton.tlb.TlbCodec

public object AnyTlbConstructor : TlbCodec<Cell> {
    override fun storeTlb(builder: CellBuilder, value: Cell) {
        builder.storeBitString(value.bits)
        builder.storeRefs(value.refs)
    }

    override fun loadTlb(slice: CellSlice): Cell {
        return buildCell {
            storeBitString(slice.loadBitString(slice.bits.size - slice.bitsPosition))
            storeRefs(slice.loadRefs(slice.refs.size - slice.refsPosition))
        }
    }
}

public object RemainingTlbCodec : TlbCodec<CellSlice> {
    override fun storeTlb(builder: CellBuilder, value: CellSlice, context: CellContext) {
        builder.storeBitString(value.bits)
        builder.storeRefs(value.refs)
    }

    override fun loadTlb(slice: CellSlice): CellSlice {
        return buildCell {
            storeBitString(slice.loadBitString(slice.bits.size - slice.bitsPosition))
            storeRefs(slice.loadRefs(slice.refs.size - slice.refsPosition))
        }.beginParse()
    }
}

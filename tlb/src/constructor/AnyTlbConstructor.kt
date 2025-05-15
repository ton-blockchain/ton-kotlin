package org.ton.kotlin.tlb.constructor

import org.ton.kotlin.cell.*
import org.ton.kotlin.tlb.TlbCodec

public object AnyTlbConstructor : TlbCodec<Cell> {
    override fun storeTlb(cellBuilder: CellBuilder, value: Cell) {
        cellBuilder.storeBits(value.bits)
        cellBuilder.storeRefs(value.refs)
    }

    override fun loadTlb(cellSlice: CellSlice): Cell {
        return buildCell {
            storeBits(cellSlice.loadBits(cellSlice.bits.size - cellSlice.bitsPosition))
            storeRefs(cellSlice.loadRefs(cellSlice.refs.size - cellSlice.refsPosition))
        }
    }
}

public object RemainingTlbCodec : TlbCodec<CellSlice> {
    override fun storeTlb(builder: CellBuilder, value: CellSlice, context: CellContext) {
        builder.storeBitString(value.bits)
        builder.storeRefs(value.refs)
    }

    override fun loadTlb(cellSlice: CellSlice): CellSlice {
        return buildCell {
            storeBitString(cellSlice.loadBits(cellSlice.bits.size - cellSlice.bitsPosition))
            storeRefs(cellSlice.loadRefs(cellSlice.refs.size - cellSlice.refsPosition))
        }.beginParse()
    }
}

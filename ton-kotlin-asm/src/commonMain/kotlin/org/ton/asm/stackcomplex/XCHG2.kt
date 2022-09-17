package org.ton.asm.stackcomplex

import org.ton.asm.AsmInstruction
import org.ton.bigint.toUByte
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider

data class XCHG2(
    val i: UByte,
    val j: UByte
) : AsmInstruction {
    override fun toString(): String = "s$i s$j XCHG2"

    companion object : TlbConstructorProvider<XCHG2> by XCHG2TlbConstructor
}

private object XCHG2TlbConstructor : TlbConstructor<XCHG2>(
    schema = "asm_xchg2#50 i:uint4 j:uint4 = XCHG2;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: XCHG2) {
        cellBuilder.storeUInt(value.i, 4)
        cellBuilder.storeUInt(value.j, 4)
    }

    override fun loadTlb(cellSlice: CellSlice): XCHG2 {
        val i = cellSlice.loadUInt(4).toUByte()
        val j = cellSlice.loadUInt(4).toUByte()
        return XCHG2(i, j)
    }
}

package org.ton.asm.stackbasic

import org.ton.asm.AsmInstruction
import org.ton.bigint.toUByte
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider

data class PUSH(
    val i: UByte
) : AsmInstruction {
    override fun toString(): String = "s$i PUSH"

    companion object : TlbConstructorProvider<PUSH> by PUSHTlbConstructor
}

private object PUSHTlbConstructor : TlbConstructor<PUSH>(
    schema = "asm_push#2 i:uint4 = PUSH;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: PUSH) {
        cellBuilder.storeUInt(value.i, 4)
    }

    override fun loadTlb(cellSlice: CellSlice): PUSH {
        val i = cellSlice.loadUInt(4).toUByte()
        return PUSH(i)
    }
}

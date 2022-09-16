package org.ton.asm.cellparse

import org.ton.asm.AsmInstruction
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider

object SCHKBITS : AsmInstruction, TlbConstructorProvider<SCHKBITS> by SCHKBITSTlbConstructor {
    override fun toString(): String = "SCHKBITS"
}

private object SCHKBITSTlbConstructor : TlbConstructor<SCHKBITS>(
    schema = "asm_schkbits#d741 = SCHKBITS;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: SCHKBITS) {
    }

    override fun loadTlb(cellSlice: CellSlice): SCHKBITS = SCHKBITS
}

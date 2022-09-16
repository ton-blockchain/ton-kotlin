package org.ton.asm.codepage

import org.ton.asm.AsmInstruction
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider

object SETCP0 : AsmInstruction, TlbConstructorProvider<SETCP0> by SETCP0TlbConstructor {
    override fun toString(): String = "SETCP0"
}

private object SETCP0TlbConstructor : TlbConstructor<SETCP0>(
    schema = "asm_setcp0#ff00 = SETCP0;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: SETCP0) {
    }

    override fun loadTlb(cellSlice: CellSlice): SETCP0 = SETCP0
}

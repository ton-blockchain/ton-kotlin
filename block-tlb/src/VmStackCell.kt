package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.Cell
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


@SerialName("vm_stk_cell")
public data class VmStackCell(
    val cell: Cell
) : VmStackValue {
    override fun toString(): String = "(vm_stk_cell cont:$cell)"

    public companion object : TlbConstructorProvider<VmStackCell> by VmStackValueCellConstructor
}

private object VmStackValueCellConstructor : TlbConstructor<VmStackCell>(
    schema = "vm_stk_cell#03 cell:^Cell = VmStackValue;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: VmStackCell
    ) = cellBuilder {
        storeRef(value.cell)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmStackCell = cellSlice {
        val cell = loadRef()
        VmStackCell(cell)
    }
}

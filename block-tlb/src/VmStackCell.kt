package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider

@Serializable
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

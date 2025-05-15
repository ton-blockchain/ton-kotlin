package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.Cell
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


@SerialName("vm_stk_builder")
public class VmStackBuilder(
    public val cell: Cell
) : VmStackValue {
    public constructor(cellBuilder: CellBuilder) : this(cellBuilder.endCell())

    public fun toCellBuilder(): CellBuilder = CellBuilder(cell)

    override fun toString(): String = "(vm_stk_builder cell:$cell)"

    public companion object : TlbConstructorProvider<VmStackBuilder> by VmStackValueBuilderTlbConstructor
}

private object VmStackValueBuilderTlbConstructor : TlbConstructor<VmStackBuilder>(
    schema = "vm_stk_builder#05 cell:^Cell = VmStackValue;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: VmStackBuilder
    ) = cellBuilder {
        storeRef(value.cell)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmStackBuilder = cellSlice {
        val cell = loadRef()
        VmStackBuilder(cell)
    }
}

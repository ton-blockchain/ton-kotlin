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

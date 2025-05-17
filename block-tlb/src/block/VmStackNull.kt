package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider

@SerialName("vm_stk_null")

public object VmStackNull : VmStackValue, TlbConstructorProvider<VmStackNull> by VmStackValueNullConstructor {
    override fun toString(): String = "vm_stk_null"
}

private object VmStackValueNullConstructor : TlbConstructor<VmStackNull>(
    schema = "vm_stk_null#00 = VmStackValue;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: VmStackNull
    ) {
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmStackNull = VmStackNull
}

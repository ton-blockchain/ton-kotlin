package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider

@SerialName("vm_stk_null")

public object VmStackNull : VmStackValue, TlbConstructorProvider<VmStackNull> by VmStackValueNullConstructor {
    override fun toString(): String = "vm_stk_null"
}

private object VmStackValueNullConstructor : TlbConstructor<VmStackNull>(
    schema = "vm_stk_null#00 = VmStackValue;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: VmStackNull
    ) {
    }

    override fun loadTlb(
        slice: CellSlice
    ): VmStackNull = VmStackNull
}

package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb


@SerialName("vm_stk_cont")
public data class VmStackCont(
    val cont: VmCont
) : VmStackValue {
    override fun toString(): String = "(vm_stk_cont cont:$cont)"

    public companion object : TlbConstructorProvider<VmStackCont> by VmStackValueContTlbConstructor
}

private object VmStackValueContTlbConstructor : TlbConstructor<VmStackCont>(
    schema = "vm_stk_cont#06 cont:VmCont = VmStackValue;"
) {
    override fun storeTlb(
        builder: CellBuilder, value: VmStackCont
    ) = builder {
        storeTlb(VmCont, value.cont)
    }

    override fun loadTlb(
        slice: CellSlice
    ): VmStackCont = slice {
        val cont = loadTlb(VmCont)
        VmStackCont(cont)
    }
}

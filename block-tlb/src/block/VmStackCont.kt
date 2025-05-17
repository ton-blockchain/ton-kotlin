package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb


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
        cellBuilder: CellBuilder, value: VmStackCont
    ) = cellBuilder {
        storeTlb(VmCont, value.cont)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmStackCont = cellSlice {
        val cont = loadTlb(VmCont)
        VmStackCont(cont)
    }
}

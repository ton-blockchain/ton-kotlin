package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbConstructor

@SerialName("vmc_quit_exc")

public object VmContQuitExc : VmCont {
    public fun tlbConstructor(): TlbConstructor<VmContQuitExc> = VmContQuitExcTlbConstructor
}

private object VmContQuitExcTlbConstructor : TlbConstructor<VmContQuitExc>(
    schema = "vmc_quit_exc\$1001 = VmCont;"
) {
    override fun storeTlb(
        builder: CellBuilder, value: VmContQuitExc
    ) = Unit

    override fun loadTlb(
        slice: CellSlice
    ): VmContQuitExc = VmContQuitExc
}

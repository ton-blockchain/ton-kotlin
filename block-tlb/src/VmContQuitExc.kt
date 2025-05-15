package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbConstructor

@SerialName("vmc_quit_exc")

public object VmContQuitExc : VmCont {
    public fun tlbConstructor(): TlbConstructor<VmContQuitExc> = VmContQuitExcTlbConstructor
}

private object VmContQuitExcTlbConstructor : TlbConstructor<VmContQuitExc>(
    schema = "vmc_quit_exc\$1001 = VmCont;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder, value: VmContQuitExc
    ) = Unit

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmContQuitExc = VmContQuitExc
}

package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbConstructor

@SerialName("vmc_quit")

public data class VmContQuit(
    val exit_code: Int
) : VmCont {
    public companion object {
        public fun tlbConstructor(): TlbConstructor<VmContQuit> = VmContQuitTlbConstructor
    }
}

private object VmContQuitTlbConstructor : TlbConstructor<VmContQuit>(
    schema = "vmc_quit\$1000 exit_code:int32 = VmCont;"
) {
    override fun storeTlb(
        builder: CellBuilder, value: VmContQuit
    ) = builder {
        storeInt(value.exit_code, 32)
    }

    override fun loadTlb(
        slice: CellSlice
    ): VmContQuit = slice {
        val exitCode = loadInt(32).toInt()
        VmContQuit(exitCode)
    }
}

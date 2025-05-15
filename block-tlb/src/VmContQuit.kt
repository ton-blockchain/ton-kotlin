package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor

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
        cellBuilder: CellBuilder, value: VmContQuit
    ) = cellBuilder {
        storeInt(value.exit_code, 32)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmContQuit = cellSlice {
        val exitCode = loadInt(32).toInt()
        VmContQuit(exitCode)
    }
}

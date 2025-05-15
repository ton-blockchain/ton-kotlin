package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.storeTlb
import kotlin.jvm.JvmStatic

@SerialName("vmc_std")

public data class VmContStd(
    val cdata: VmControlData,
    val code: VmCellSlice
) : VmCont {
    public companion object : TlbCodec<VmContStd> by VmContStdTlbConstructor {
        @JvmStatic
        public fun tlbCodec(): TlbConstructor<VmContStd> = VmContStdTlbConstructor
    }
}

private object VmContStdTlbConstructor : TlbConstructor<VmContStd>(
    schema = "vmc_std\$00 cdata:VmControlData code:VmCellSlice = VmCont;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder, value: VmContStd
    ) = cellBuilder {
        storeTlb(VmControlData, value.cdata)
        storeTlb(VmCellSlice, value.code)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmContStd = cellSlice {
        val cdata = loadTlb(VmControlData)
        val code = loadTlb(VmCellSlice)
        VmContStd(cdata, code)
    }
}

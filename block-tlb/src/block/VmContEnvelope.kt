package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.CellRef
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.storeTlb
import kotlin.jvm.JvmStatic

@SerialName("vmc_envelope")

public data class VmContEnvelope(
    val cdata: VmControlData,
    val next: CellRef<VmCont>
) : VmCont {
    public companion object {
        @JvmStatic
        public fun tlbCodec(): TlbConstructor<VmContEnvelope> = VmContEnvelopeTlbConstructor()
    }
}

private class VmContEnvelopeTlbConstructor : TlbConstructor<VmContEnvelope>(
    schema = "vmc_envelope\$01 cdata:VmControlData next:^VmCont = VmCont;"
) {
    private val vmContCodec = CellRef.tlbCodec(VmCont)

    override fun storeTlb(
        cellBuilder: CellBuilder, value: VmContEnvelope
    ) = cellBuilder {
        storeTlb(VmControlData, value.cdata)
        storeTlb(vmContCodec, value.next)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmContEnvelope = cellSlice {
        val cdata = loadTlb(VmControlData)
        val next = loadTlb(vmContCodec)
        VmContEnvelope(cdata, next)
    }
}

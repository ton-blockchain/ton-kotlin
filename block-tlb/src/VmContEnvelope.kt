package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.CellRef
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb
import kotlin.jvm.JvmStatic

@SerialName("vmc_envelope")
@Serializable
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

package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.sdk.bigint.toLong
import org.ton.tlb.CellRef
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

@SerialName("vmc_repeat")

public data class VmContRepeat(
    val count: Long,
    val body: CellRef<VmCont>,
    val after: CellRef<VmCont>
) : VmCont {
    public companion object {
        public fun tlbConstructor(): TlbConstructor<VmContRepeat> = VmContRepeatTlbConstructor
    }
}

private object VmContRepeatTlbConstructor : TlbConstructor<VmContRepeat>(
    schema = "vmc_repeat\$10100 count:uint63 body:^VmCont after:^VmCont = VmCont;"
) {
    private val vmCont = CellRef.tlbCodec(VmCont)

    override fun storeTlb(
        builder: CellBuilder, value: VmContRepeat
    ) = builder {
        storeUInt(value.count, 63)
        storeTlb(vmCont, value.body)
        storeTlb(vmCont, value.after)
    }

    override fun loadTlb(
        slice: CellSlice
    ): VmContRepeat = slice {
        val count = loadUInt(63).toLong()
        val body = loadTlb(vmCont)
        val after = loadTlb(vmCont)
        VmContRepeat(count, body, after)
    }
}

package org.ton.kotlin.block

import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.hashmap.HashMapE
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import kotlin.jvm.JvmStatic


public data class VmSaveList(
    val cregs: HashMapE<VmStackValue>
) {
    public companion object : TlbCodec<VmSaveList> by VmSaveListTlbConstructor {
        @JvmStatic
        public fun tlbCodec(): TlbConstructor<VmSaveList> = VmSaveListTlbConstructor
    }
}

private object VmSaveListTlbConstructor : TlbConstructor<VmSaveList>(
    schema = "_ cregs:(HashmapE 4 VmStackValue) = VmSaveList;"
) {
    private val hashmapCombinator = HashMapE.tlbCodec(4, VmStackValue)

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: VmSaveList
    ) = cellBuilder {
        hashmapCombinator.storeTlb(this, value.cregs)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmSaveList = cellSlice {
        val creg = loadTlb(hashmapCombinator)
        VmSaveList(creg)
    }
}

package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb


@SerialName("vm_stk_tuple")
public class VmStackTuple(
    public val len: Int,
    public val data: VmTuple
) : VmStackValue {
    public constructor(data: VmTuple) : this(data.depth(), data)

    override fun toString(): String = "(vm_stk_tuple len:$len data:$data)"

    public companion object : TlbConstructorProvider<VmStackTuple> by VmStackValueTupleConstructor
}

private object VmStackValueTupleConstructor : TlbConstructor<VmStackTuple>(
    schema = "vm_stk_tuple#07 len:(## 16) data:(VmTuple len) = VmStackValue;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder, value: VmStackTuple
    ) = cellBuilder {
        storeUInt(value.len, 16)
        storeTlb(VmTuple.tlbCodec(value.len), value.data)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmStackTuple = cellSlice {
        val len = loadUInt(16).toInt()
        val data = loadTlb(VmTuple.tlbCodec(len))
        VmStackTuple(len, data)
    }
}

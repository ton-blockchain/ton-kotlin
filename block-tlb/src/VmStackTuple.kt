package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb


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
        builder: CellBuilder, value: VmStackTuple
    ) = builder {
        storeUInt(value.len, 16)
        storeTlb(VmTuple.tlbCodec(value.len), value.data)
    }

    override fun loadTlb(
        slice: CellSlice
    ): VmStackTuple = slice {
        val len = loadUInt(16).toInt()
        val data = loadTlb(VmTuple.tlbCodec(len))
        VmStackTuple(len, data)
    }
}

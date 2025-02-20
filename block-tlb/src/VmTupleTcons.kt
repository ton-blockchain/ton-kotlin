package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb
import kotlin.jvm.JvmStatic

@SerialName("vm_tuple_tcons")

public data class VmTupleTcons(
    val head: VmTupleRef,
    val tail: VmStackValue
) : VmTuple {
    override fun depth(): Int = head.depth() + 1

    override fun toString(): String = "(vm_tuple_tcons head:$head tail:$tail)"

    public companion object {
        @JvmStatic
        public fun tlbCodec(n: Int): TlbConstructor<VmTupleTcons> = VmTupleTconsTlbConstructor(n)
    }
}

private class VmTupleTconsTlbConstructor(n: Int) : TlbConstructor<VmTupleTcons>(
    schema = "vm_tuple_tcons\$_ {n:#} head:(VmTupleRef n) tail:^VmStackValue = VmTuple (n + 1);"
) {
    private val vmTupleRef = VmTupleRef.tlbCodec(n - 1)

    override fun storeTlb(builder: CellBuilder, value: VmTupleTcons) = builder {
        storeTlb(vmTupleRef, value.head)
        storeTlb(VmStackValue, value.tail)
    }

    override fun loadTlb(slice: CellSlice): VmTupleTcons = slice {
        val head = loadTlb(vmTupleRef)
        val tail = loadTlb(VmStackValue)
        VmTupleTcons(head, tail)
    }
}

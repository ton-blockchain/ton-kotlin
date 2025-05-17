package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.storeTlb
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

    override fun storeTlb(cellBuilder: CellBuilder, value: VmTupleTcons) = cellBuilder {
        storeTlb(vmTupleRef, value.head)
        storeTlb(VmStackValue, value.tail)
    }

    override fun loadTlb(cellSlice: CellSlice): VmTupleTcons = cellSlice {
        val head = loadTlb(vmTupleRef)
        val tail = loadTlb(VmStackValue)
        VmTupleTcons(head, tail)
    }
}

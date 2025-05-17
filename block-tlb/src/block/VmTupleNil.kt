package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider

@SerialName("vm_tuple_nil")

public object VmTupleNil : VmTuple, TlbConstructorProvider<VmTupleNil> by VmTupleNilTlbConstructor {
    override fun depth(): Int = 0

    override fun toString(): String = "vm_tuple_nil"
}

private object VmTupleNilTlbConstructor : TlbConstructor<VmTupleNil>(
    schema = "vm_tuple_nil\$_ = VmTuple 0;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: VmTupleNil) {
    }

    override fun loadTlb(cellSlice: CellSlice): VmTupleNil = VmTupleNil
}

package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.*
import org.ton.sdk.bigint.toInt
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider

@SerialName("vm_stk_slice")

public data class VmCellSlice(
    override val cell: Cell,
    override val stBits: Int,
    override val endBits: Int,
    override val stRef: Int,
    override val endRef: Int
) : VmStackSlice {
    public constructor(cellSlice: CellSlice) : this(
        cell = buildCell {
            storeBitString(cellSlice.bits)
            storeRefs(cellSlice.refs)
        },
        stBits = cellSlice.bitsPosition,
        endBits = cellSlice.bits.size,
        stRef = cellSlice.refsPosition,
        endRef = cellSlice.refs.size
    )

    override fun toString(): String =
        "(vm_stk_slice cell:${if (stRef == 0 && endRef == 0) cell.bits.toString() else cell.toString()} st_bits:$stBits end_bits:$endBits st_ref:$stRef end_ref:$endRef)"

    public companion object : TlbConstructorProvider<VmCellSlice> by VmCellSliceTlbConstructor
}

private object VmCellSliceTlbConstructor : TlbConstructor<VmCellSlice>(
    schema = "vm_stk_slice#04 cell:^Cell st_bits:(## 10) end_bits:(## 10) { st_bits <= end_bits } " +
            "st_ref:(#<= 4) end_ref:(#<= 4) { st_ref <= end_ref } = VmCellSlice;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: VmCellSlice
    ) = builder {
        storeRef(value.cell)
        storeUInt(value.stBits, 10)
        storeUInt(value.endBits, 10)
        storeUIntLeq(value.stRef, 4)
        storeUIntLeq(value.endRef, 4)
    }

    override fun loadTlb(
        slice: CellSlice
    ): VmCellSlice = slice {
        val cell = loadRef()
        val stBits = loadUInt(10).toInt()
        val endBits = loadUInt(10).toInt()
        val stRef = loadUIntLeq(4).toInt()
        val endRef = loadUIntLeq(4).toInt()
        VmCellSlice(cell, stBits, endBits, stRef, endRef)
    }
}

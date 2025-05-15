package org.ton.kotlin.block

import org.ton.bigint.BigInt
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider

public object VmStackNan : VmStackValue, VmStackNumber,
    TlbConstructorProvider<VmStackNan> by VmStackValueNanConstructor {
    override fun toInt(): Int = throw VmStackNanException()
    override fun toLong(): Long = throw VmStackNanException()
    override fun toBigInt(): BigInt = throw VmStackNanException()
    override fun toBoolean(): Boolean = throw VmStackNanException()

    override fun plus(other: VmStackNumber): VmStackNumber = throw VmStackNanException()
    override fun minus(other: VmStackNumber): VmStackNumber = throw VmStackNanException()
    override fun times(other: VmStackNumber): VmStackNumber = throw VmStackNanException()
    override fun div(other: VmStackNumber): VmStackNumber = throw VmStackNanException()

    public class VmStackNanException : RuntimeException("NaN exception")

    override fun toString(): String = "vm_stk_nan"
}

private object VmStackValueNanConstructor : TlbConstructor<VmStackNan>(
    schema = "vm_stk_nan#02ff = VmStackValue;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: VmStackNan
    ) {
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmStackNan = VmStackNan
}

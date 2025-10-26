package org.ton.block

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.sdk.bigint.BigInt
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider

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
        builder: CellBuilder,
        value: VmStackNan
    ) {
    }

    override fun loadTlb(
        slice: CellSlice
    ): VmStackNan = VmStackNan
}

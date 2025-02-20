package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.bigint.*
import org.ton.block.VmStackNan.VmStackNanException
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider

@SerialName("vm_stk_int")

public data class VmStackInt(
    val value: BigInt
) : VmStackValue, VmStackNumber {
    public constructor(int: Int) : this(int.toBigInt())
    public constructor(long: Long) : this(long.toBigInt())

    override fun toInt(): Int = value.toInt()
    override fun toLong(): Long = value.toLong()
    override fun toBigInt(): BigInt = value
    override fun toBoolean(): Boolean = value != 0.toBigInt()

    override fun plus(other: VmStackNumber): VmStackNumber = when (other) {
        is VmStackInt -> VmStackInt(value + other.value)
        is VmStackTinyInt -> VmStackInt(value + other.value.toBigInt())
        VmStackNan -> throw VmStackNanException()
    }

    override fun minus(other: VmStackNumber): VmStackNumber = when (other) {
        is VmStackInt -> VmStackInt(value - other.value)
        is VmStackTinyInt -> VmStackInt(value - other.value.toBigInt())
        VmStackNan -> throw VmStackNanException()
    }

    override fun times(other: VmStackNumber): VmStackNumber = when (other) {
        is VmStackInt -> VmStackInt(value * other.value)
        is VmStackTinyInt -> VmStackInt(value * other.value.toBigInt())
        VmStackNan -> throw VmStackNanException()
    }

    override fun div(other: VmStackNumber): VmStackNumber = when (other) {
        is VmStackInt -> VmStackInt(value / other.value)
        is VmStackTinyInt -> VmStackInt(value / other.value.toBigInt())
        VmStackNan -> throw VmStackNanException()
    }

    override fun toString(): String = "(vm_stk_int value:$value)"

    public companion object : TlbConstructorProvider<VmStackInt> by VmStackIntTlbConstructor
}

private object VmStackIntTlbConstructor : TlbConstructor<VmStackInt>(
    schema = "vm_stk_int#0201_ value:int257 = VmStackValue;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: VmStackInt
    ) = builder {
        storeInt(value.value, 257)
    }

    override fun loadTlb(
        slice: CellSlice
    ): VmStackInt = slice {
        val value = loadInt(257)
        VmStackInt(value)
    }
}

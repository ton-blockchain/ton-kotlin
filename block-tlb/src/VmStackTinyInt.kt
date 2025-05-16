package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.bigint.*
import org.ton.kotlin.block.VmStackNan.VmStackNanException
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import kotlin.math.abs

@SerialName("vm_stk_tinyint")

public data class VmStackTinyInt(
    val value: Long
) : VmStackValue, VmStackNumber {
    public constructor(int: Int) : this(int.toLong())
    public constructor(boolean: Boolean) : this(if (boolean) -1 else 0)

    override fun toInt(): Int = value.toInt()
    override fun toLong(): Long = value
    override fun toBigInt(): BigInt = value.toBigInt()
    override fun toBoolean(): Boolean = value != 0L

    override fun plus(other: VmStackNumber): VmStackNumber {
        return when (other) {
            is VmStackTinyInt -> {
                // HD 2-12 Overflow iff both arguments have the opposite sign of the result
                val r = value + other.value
                if (((value xor r) and (other.value xor r)) < 0) {
                    VmStackInt(value.toBigInt() + other.value.toBigInt())
                } else {
                    VmStackTinyInt(r)
                }
            }

            is VmStackInt -> VmStackInt(other.value + value.toBigInt())
            VmStackNan -> throw VmStackNanException()
        }
    }

    override fun minus(other: VmStackNumber): VmStackNumber = when (other) {
        is VmStackTinyInt -> {
            val r = value - other.value
            // HD 2-12 Overflow iff the arguments have different signs and
            // the sign of the result is different from the sign of x
            if ((value xor other.value) and (value xor r) < 0) {
                VmStackTinyInt(r)
            } else {
                VmStackInt(value.toBigInt() - other.value.toBigInt())
            }
        }

        is VmStackInt -> VmStackInt(other.value + value.toBigInt())
        VmStackNan -> throw VmStackNanException()
    }

    override fun times(other: VmStackNumber): VmStackNumber {
        return when (other) {
            is VmStackTinyInt -> {
                val r = value * other.value
                val ax = abs(value)
                val ay = abs(other.value)
                if (((ax or ay) ushr 31) != 0L) {
                    // Some bits greater than 2^31 that might cause overflow
                    // Check the result using the divide operator
                    // and check for the special case of Long.MIN_VALUE * -1
                    if (((other.value != 0L) and (r / other.value != value)) ||
                        (value == Long.MAX_VALUE && other.value == -1L)
                    ) {
                        return VmStackInt(value.toBigInt() * other.value.toBigInt())
                    }
                }
                VmStackTinyInt(r)
            }

            is VmStackInt -> VmStackInt(value.toBigInt() * other.value)
            VmStackNan -> throw VmStackNanException()
        }
    }

    override fun div(other: VmStackNumber): VmStackNumber = when (other) {
        is VmStackTinyInt -> VmStackTinyInt(value / other.value)
        is VmStackInt -> VmStackInt(value.toBigInt() / other.value)
        is VmStackNan -> throw VmStackNanException()
    }

    override fun toString(): String = "(vm_stk_tinyint value:$value)"

    public companion object : TlbConstructorProvider<VmStackTinyInt> by VmStackTinyIntTlbConstructor
}

private object VmStackTinyIntTlbConstructor : TlbConstructor<VmStackTinyInt>(
    schema = "vm_stk_tinyint#01 value:int64 = VmStackValue;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: VmStackTinyInt
    ) = cellBuilder {
        storeInt(value.value, 64)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmStackTinyInt = cellSlice {
        val value = loadInt(64).toLong()
        VmStackTinyInt(value)
    }
}

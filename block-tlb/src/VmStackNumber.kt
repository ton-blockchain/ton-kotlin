package org.ton.kotlin.block

import org.ton.kotlin.bigint.BigInt

public sealed interface VmStackNumber : VmStackValue {
    public fun toInt(): Int
    public fun toLong(): Long
    public fun toBoolean(): Boolean
    public fun toBigInt(): BigInt

    public operator fun plus(other: VmStackNumber): VmStackNumber
    public operator fun minus(other: VmStackNumber): VmStackNumber
    public operator fun times(other: VmStackNumber): VmStackNumber
    public operator fun div(other: VmStackNumber): VmStackNumber
}

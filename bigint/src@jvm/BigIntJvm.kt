package org.ton.bigint

import java.math.BigInteger

public actual class BigInt internal constructor(
    internal val value: BigInteger
) : Number(), Comparable<BigInt> {
    public actual constructor(string: String) : this(BigInteger(string))

    public actual constructor(string: String, radix: Int) : this(BigInteger(string, radix))

    public actual constructor(byteArray: ByteArray) : this(BigInteger(byteArray))

    public actual fun toByteArray(): ByteArray =
        value.toByteArray()

    public actual fun toString(radix: Int): String =
        value.toString(radix)

    actual override fun compareTo(other: BigInt): Int =
        value.compareTo(other.value)

    actual override fun toByte(): Byte =
        value.toByte()

    @Deprecated(
        "Direct conversion to Char is deprecated. Use toInt().toChar() or Char constructor instead.\nIf you override toChar() function in your Number inheritor, it's recommended to gradually deprecate the overriding function and then remove it.\nSee https://youtrack.jetbrains.com/issue/KT-46465 for details about the migration",
        replaceWith = ReplaceWith("this.value.toInt().toChar()")
    )
    override fun toChar(): Char =
        value.toInt().toChar()

    actual override fun toDouble(): Double =
        value.toDouble()

    actual override fun toFloat(): Float =
        value.toFloat()

    actual override fun toInt(): Int =
        value.toInt()

    actual override fun toLong(): Long =
        value.toLong()

    actual override fun toShort(): Short =
        value.toShort()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BigInt) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}

public actual fun Int.toBigInt(): BigInt =
    BigInt(BigInteger.valueOf(this.toLong()))

public actual fun Long.toBigInt(): BigInt =
    BigInt(BigInteger.valueOf(this))

public actual val BigInt.bitLength: Int
    get() = value.bitLength()


public actual val BigInt.sign: Int
    get() =
        value.signum()
public actual val BigInt.isZero: Boolean
    get() = value == BigInteger.ZERO

public actual operator fun BigInt.plus(other: BigInt): BigInt =
    BigInt(value + other.value)

public actual operator fun BigInt.minus(other: BigInt): BigInt =
    BigInt(value - other.value)

public actual operator fun BigInt.times(other: BigInt): BigInt =
    BigInt(value * other.value)

public actual operator fun BigInt.div(other: BigInt): BigInt =
    BigInt(value / other.value)

public actual operator fun BigInt.unaryMinus(): BigInt =
    BigInt(-value)

public actual operator fun BigInt.rem(other: BigInt): BigInt =
    BigInt(value % other.value)

public actual infix fun BigInt.shr(shr: Int): BigInt =
    BigInt(value shr shr)

public actual infix fun BigInt.shl(shl: Int): BigInt =
    BigInt(value shl shl)

public actual infix fun BigInt.and(and: BigInt): BigInt =
    BigInt(value and and.value)

public actual infix fun BigInt.or(mod: BigInt): BigInt =
    BigInt(value or mod.value)

public actual infix fun BigInt.xor(mod: BigInt): BigInt =
    BigInt(value xor mod.value)

public actual fun BigInt.not(): BigInt =
    BigInt(value.not())

public actual infix fun BigInt.divRem(other: BigInt): Pair<BigInt, BigInt> {
    val result = other.value.divideAndRemainder(other.value)
    return BigInt(result[0]) to BigInt(result[1])
}

public actual infix fun BigInt.pow(pow: Int): BigInt =
    BigInt(value.pow(pow))

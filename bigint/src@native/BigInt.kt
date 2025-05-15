package org.ton.kotlin.bigint

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.util.fromTwosComplementByteArray

@Suppress("ConvertSecondaryConstructorToPrimary")
public actual class BigInt internal constructor(
    internal val value: BigInteger
) : Number(), Comparable<BigInt> {
    public actual constructor(string: String) : this(string, 10)

    public actual constructor(string: String, radix: Int) : this(BigInteger.parseString(string, radix))

    public actual constructor(byteArray: ByteArray) : this(BigInteger.fromTwosComplementByteArray(byteArray))

    public actual fun toByteArray(): ByteArray =
        value.toByteArray()

    public actual fun toString(radix: Int): String =
        value.toString(radix)

    override fun compareTo(other: BigInt): Int =
        value.compareTo(other.value)

    override fun toByte(): Byte =
        value.byteValue()

    @Deprecated(
        "Direct conversion to Char is deprecated. Use toInt().toChar() or Char constructor instead.\nIf you override toChar() function in your Number inheritor, it's recommended to gradually deprecate the overriding function and then remove it.\nSee https://youtrack.jetbrains.com/issue/KT-46465 for details about the migration",
        replaceWith = ReplaceWith("this.value.toInt().toChar()")
    )
    override fun toChar(): Char =
        value.shortValue().toInt().toChar()

    override fun toDouble(): Double =
        value.doubleValue()

    override fun toFloat(): Float =
        value.floatValue()

    override fun toInt(): Int =
        value.intValue()

    override fun toLong(): Long =
        value.longValue()

    override fun toShort(): Short =
        value.shortValue()

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
    BigInt(BigInteger.fromInt(this))

public actual fun Long.toBigInt(): BigInt =
    BigInt(BigInteger.fromLong(this))

public actual val BigInt.bitLength: Int
    get() {
        // TODO: https://github.com/ionspin/kotlin-multiplatform-bignum/pull/254
        return if (value.isNegative) {
            if (value == BigInteger.ONE.negate()) 0
            else (value.abs() - 1).toString(2).length
        } else {
            if (value.isZero()) 0
            else value.toString(2).length
        }
    }

public actual val BigInt.sign: Int
    get() =
        value.signum()
public actual val BigInt.isZero: Boolean
    get() =
        value.isZero()

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
    return BigInt(result.first) to BigInt(result.second)
}

public actual infix fun BigInt.pow(pow: Int): BigInt =
    BigInt(value.pow(pow))

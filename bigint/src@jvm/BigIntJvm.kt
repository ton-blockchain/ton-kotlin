package org.ton.kotlin.bigint

import java.math.BigInteger

@Suppress("NOTHING_TO_INLINE")
public inline fun BigInteger.toKotlinBigInt(): BigInt = BigInt(this)

@Suppress("NOTHING_TO_INLINE")
public inline fun BigInt.toJavaBigInteger(): BigInteger = value

public actual class BigInt @PublishedApi internal constructor(
    @PublishedApi
    internal val value: BigInteger
) : Number(), Comparable<BigInt> {
    public actual constructor(string: String) : this(BigInteger(string))

    public actual constructor(string: String, radix: Int) : this(BigInteger(string, radix))

    public actual constructor(byteArray: ByteArray) : this(BigInteger(byteArray))

    actual override fun toDouble(): Double = value.toDouble()

    actual override fun toFloat(): Float = value.toFloat()

    actual override fun toLong(): Long = value.toLong()

    actual override fun toInt(): Int = value.toInt()

    actual override fun toShort(): Short = value.toShort()

    actual override fun toByte(): Byte = value.toByte()

    actual override fun compareTo(other: BigInt): Int = value.compareTo(other.value)

    public actual fun toByteArray(): ByteArray = value.toByteArray()

    public actual fun toString(radix: Int): String = value.toString(radix)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BigInt) return false
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()
}

public actual fun Int.toBigInt(): BigInt = BigInt(BigInteger.valueOf(this.toLong()))
public actual fun Long.toBigInt(): BigInt = BigInt(BigInteger.valueOf(this))

public actual val BigInt.bitLength: Int get() = value.bitLength()
public actual val BigInt.sign: Int get() = value.signum()
public actual val BigInt.isZero: Boolean get() = value == BigInteger.ZERO

public actual operator fun BigInt.plus(other: BigInt): BigInt = BigInt(value.add(other.value))
public operator fun BigInt.plus(int: Int): BigInt = BigInt(value.add(BigInteger.valueOf(int.toLong())))
public operator fun BigInt.plus(long: Long): BigInt = BigInt(value.add(BigInteger.valueOf(long)))

public actual operator fun BigInt.minus(other: BigInt): BigInt = BigInt(value.subtract(other.value))
public operator fun BigInt.minus(int: Int): BigInt = BigInt(value.subtract(BigInteger.valueOf(int.toLong())))
public operator fun BigInt.minus(long: Long): BigInt = BigInt(value.subtract(BigInteger.valueOf(long)))

public actual operator fun BigInt.times(other: BigInt): BigInt = BigInt(value.multiply(other.value))
public operator fun BigInt.times(int: Int): BigInt = BigInt(value.multiply(BigInteger.valueOf(int.toLong())))
public operator fun BigInt.times(long: Long): BigInt = BigInt(value.multiply(BigInteger.valueOf(long)))

public actual operator fun BigInt.div(other: BigInt): BigInt = BigInt(value.divide(other.value))
public operator fun BigInt.div(int: Int): BigInt = BigInt(value.divide(BigInteger.valueOf(int.toLong())))
public operator fun BigInt.div(long: Long): BigInt = BigInt(value.divide(BigInteger.valueOf(long)))

public actual operator fun BigInt.unaryMinus(): BigInt = BigInt(value.negate())

public actual infix fun BigInt.shr(shr: Int): BigInt = BigInt(value.shiftRight(shr))

public actual infix fun BigInt.shl(shl: Int): BigInt = BigInt(value.shiftLeft(shl))

public actual infix fun BigInt.and(and: BigInt): BigInt = BigInt(value.and(and.value))

public actual operator fun BigInt.rem(other: BigInt): BigInt = BigInt(value.mod(other.value))

public actual infix fun BigInt.or(mod: BigInt): BigInt = BigInt(value.or(mod.value))

public actual infix fun BigInt.xor(mod: BigInt): BigInt = BigInt(value.xor(mod.value))

public actual infix fun BigInt.pow(pow: Int): BigInt = BigInt(value.pow(pow))

public actual fun BigInt.not(): BigInt = BigInt(value.not())

public actual infix fun BigInt.divRem(other: BigInt): Pair<BigInt, BigInt> {
    val result = value.divideAndRemainder(other.value)
    return result[0].toKotlinBigInt() to result[1].toKotlinBigInt()
}

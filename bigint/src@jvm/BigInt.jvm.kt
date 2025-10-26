package org.ton.bigint

import java.math.BigInteger

public actual typealias BigInt = BigInteger

public actual fun ByteArray.toBigInt(): BigInt {
    return BigInteger(1, this)
}

public actual fun Int.toBigInt(): BigInt =
    BigInteger.valueOf(this.toLong())

public actual fun Long.toBigInt(): BigInt =
    BigInteger.valueOf(this)

public actual fun BigInt.toLong(): Long = toLong()
public actual fun BigInt.toInt(): Int = toInt()

public actual val BigInt.bitLength: Int
    get() = bitLength()

public actual val BigInt.sign: Int
    get() = signum()
public actual val BigInt.isZero: Boolean
    get() = this == BigInteger.ZERO

public actual operator fun BigInt.compareTo(other: BigInt): Int = compareTo(other)
public actual operator fun BigInt.plus(other: BigInt): BigInt = add(other)
public actual operator fun BigInt.minus(other: BigInt): BigInt = subtract(other)
public actual operator fun BigInt.times(other: BigInt): BigInt = multiply(other)
public actual operator fun BigInt.div(other: BigInt): BigInt = divide(other)
public actual operator fun BigInt.unaryMinus(): BigInt = negate()
public actual operator fun BigInt.rem(other: BigInt): BigInt = mod(other)
public actual infix fun BigInt.shr(shr: Int): BigInt = shiftRight(shr)
public actual infix fun BigInt.shl(shl: Int): BigInt = shiftLeft(shl)
public actual infix fun BigInt.and(and: BigInt): BigInt = and(and)
public actual infix fun BigInt.or(mod: BigInt): BigInt = or(mod)
public actual infix fun BigInt.xor(mod: BigInt): BigInt = xor(mod)
public actual fun BigInt.not(): BigInt = not()
public actual infix fun BigInt.divRem(other: BigInt): Pair<BigInt, BigInt> {
    val result = divideAndRemainder(other)
    return result[0] to result[1]
}

public actual infix fun BigInt.pow(pow: Int): BigInt = pow(pow)
public actual fun BigInt.toString(radix: Int): String {
    return toString(radix)
}

public actual fun String.toBigInt(radix: Int): BigInt {
    return BigInteger(this, radix)
}

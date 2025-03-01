package org.ton.bigint

import java.math.BigInteger

public actual typealias BigInt = BigInteger

public actual fun Int.toBigInt(): BigInt = BigInteger.valueOf(this.toLong())
public actual fun Long.toBigInt(): BigInt = BigInteger.valueOf(this)

public actual val BigInt.bitLength: Int get() = bitLength()
public actual val BigInt.sign: Int get() = signum()
public actual val BigInt.isZero: Boolean get() = this == BigInteger.ZERO

public actual operator fun BigInt.plus(other: BigInt): BigInt = add(other)
public operator fun BigInt.plus(int: Int): BigInt = add(int.toBigInt())
public operator fun BigInt.plus(long: Long): BigInt = add(long.toBigInt())

public actual operator fun BigInt.minus(other: BigInt): BigInt = subtract(other)
public operator fun BigInt.minus(int: Int): BigInt = subtract(int.toBigInt())
public operator fun BigInt.minus(long: Long): BigInt = subtract(long.toBigInt())

public actual operator fun BigInt.times(other: BigInt): BigInt = multiply(other)
public operator fun BigInt.times(int: Int): BigInt = multiply(int.toBigInt())
public operator fun BigInt.times(long: Long): BigInt = multiply(long.toBigInt())

public actual operator fun BigInt.div(other: BigInt): BigInt = divide(other)
public operator fun BigInt.div(int: Int): BigInt = divide(int.toBigInt())
public operator fun BigInt.div(long: Long): BigInt = divide(long.toBigInt())

public actual operator fun BigInt.unaryMinus(): BigInt = negate()

public actual operator fun BigInt.inc(): BigInt = add(BigInteger.ONE)

public actual operator fun BigInt.dec(): BigInt = subtract(BigInteger.ONE)

public actual infix fun BigInt.shr(shr: Int): BigInt = shiftRight(shr)

public actual infix fun BigInt.shl(shl: Int): BigInt = shiftLeft(shl)

public actual infix fun BigInt.and(and: BigInt): BigInt = and(and)

public actual operator fun BigInt.rem(other: BigInt): BigInt = mod(other)

public actual infix fun BigInt.or(mod: BigInt): BigInt = or(mod)

public actual infix fun BigInt.xor(mod: BigInt): BigInt = xor(mod)

public actual infix fun BigInt.pow(pow: Int): BigInt = pow(pow)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
public actual fun BigInt.not(): BigInt =
    this.not()

public actual infix fun BigInt.divRem(other: BigInt): Pair<BigInt, BigInt> {
    val result = divideAndRemainder(other)
    return result[0] to result[1]
}

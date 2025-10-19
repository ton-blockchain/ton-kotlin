package org.ton.bigint

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger

public actual typealias BigInt = BigInteger

public actual fun Int.toBigInt(): BigInt = toBigInteger()
public actual fun Long.toBigInt(): BigInt = toBigInteger()

public actual operator fun BigInt.plus(other: BigInt): BigInt = plus(other)
public actual operator fun BigInt.minus(other: BigInt): BigInt = minus(other)
public actual operator fun BigInt.times(other: BigInt): BigInt = times(other)
public actual operator fun BigInt.div(other: BigInt): BigInt = div(other)
public actual operator fun BigInt.unaryMinus(): BigInt = unaryMinus()
public actual operator fun BigInt.rem(other: BigInt): BigInt = rem(other)
public actual infix fun BigInt.shr(shr: Int): BigInt = shr(shr)
public actual infix fun BigInt.shl(shl: Int): BigInt = shl(shl)
public actual infix fun BigInt.and(and: BigInt): BigInt = and(and)
public actual infix fun BigInt.or(mod: BigInt): BigInt = or(mod)
public actual infix fun BigInt.xor(mod: BigInt): BigInt = xor(mod)
public actual infix fun BigInt.divRem(other: BigInt): Pair<BigInt, BigInt> = divideAndRemainder(other)
public actual infix fun BigInt.pow(pow: Int): BigInt = pow(pow)
public actual fun BigInt.not(): BigInt = not()

public actual val BigInt.bitLength: Int
    get() {
        // TODO: https://github.com/ionspin/kotlin-multiplatform-bignum/pull/254
        return if (isNegative) {
            if (this == BigInteger.ONE.negate()) 0
            else (abs() - 1).toString(2).length
        } else {
            if (isZero()) 0
            else toString(2).length
        }
    }
public actual val BigInt.sign: Int
    get() = signum()
public actual val BigInt.isZero: Boolean
    get() = isZero()

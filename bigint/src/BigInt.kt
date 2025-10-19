package org.ton.bigint

public expect class BigInt

public expect fun Int.toBigInt(): BigInt
public expect fun Long.toBigInt(): BigInt

public expect fun BigInt.toString(radix: Int): String
public expect fun String.toBigInt(radix: Int = 10): BigInt

public expect operator fun BigInt.plus(other: BigInt): BigInt
public expect operator fun BigInt.minus(other: BigInt): BigInt
public expect operator fun BigInt.times(other: BigInt): BigInt
public expect operator fun BigInt.div(other: BigInt): BigInt
public expect operator fun BigInt.unaryMinus(): BigInt
public expect operator fun BigInt.rem(other: BigInt): BigInt
public expect infix fun BigInt.shr(shr: Int): BigInt
public expect infix fun BigInt.shl(shl: Int): BigInt
public expect infix fun BigInt.and(and: BigInt): BigInt
public expect infix fun BigInt.or(mod: BigInt): BigInt
public expect infix fun BigInt.xor(mod: BigInt): BigInt
public expect infix fun BigInt.divRem(other: BigInt): Pair<BigInt, BigInt>
public expect infix fun BigInt.pow(pow: Int): BigInt
public expect fun BigInt.not(): BigInt

public expect val BigInt.bitLength: Int
public expect val BigInt.sign: Int
public expect val BigInt.isZero: Boolean

package org.ton.bigint

public expect class BigInt : Comparable<BigInt>, Number {
    public constructor(string: String)
    public constructor(string: String, radix: Int)
    public constructor(byteArray: ByteArray)

    public fun toByteArray(): ByteArray
    public fun toString(radix: Int): String
    override fun compareTo(other: BigInt): Int
    override fun toDouble(): Double
    override fun toFloat(): Float
    override fun toLong(): Long
    override fun toInt(): Int
    override fun toShort(): Short
    override fun toByte(): Byte
}

public expect fun Int.toBigInt(): BigInt
public expect fun Long.toBigInt(): BigInt

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

public operator fun BigInt.compareTo(other: Int): Int = compareTo(other.toBigInt())
public operator fun BigInt.compareTo(other: Long): Int = compareTo(other.toBigInt())

public expect val BigInt.bitLength: Int
public expect val BigInt.sign: Int
public expect val BigInt.isZero: Boolean

@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.currency

import org.ton.kotlin.bigint.BigInt
import org.ton.kotlin.bigint.bitLength
import org.ton.kotlin.bigint.sign
import org.ton.kotlin.bigint.toBigInt
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbCodec

/**
 * Variable-length 248-bit integer. Used for extra currencies.
 *
 * Stored as 5 bits of `len` (0..=31), followed by `len` bytes.
 *
 * @see [org.ton.kotlin.block.ExtraCurrencyCollection]
 */
public data class VarUInt248(
    public val amount: BigInt
) : Number(), Comparable<VarUInt248> {
    init {
        require(amount.sign != -1) { "Amount must be less than zero." }
        require(amount.bitLength <= 248) { "Amount overflow" }
    }

    public constructor(amount: Long) : this(amount.toBigInt())

    public constructor(amount: String, radix: Int) : this(BigInt(amount, radix))

    override fun toDouble(): Double = amount.toDouble()
    override fun toFloat(): Float = amount.toFloat()
    override fun toLong(): Long = amount.toLong()
    override fun toInt(): Int = amount.toInt()
    override fun toShort(): Short = amount.toShort()
    override fun toByte(): Byte = amount.toByte()

    override fun compareTo(other: VarUInt248): Int {
        return amount.compareTo(other.amount)
    }

    override fun toString(): String = amount.toString()

    public companion object : TlbCodec<VarUInt248> by VarUInt248Codec {
        public val ZERO: VarUInt248 = VarUInt248(0.toBigInt())
        public val ONE: VarUInt248 = VarUInt248(1.toBigInt())
        public val TWO: VarUInt248 = VarUInt248(2.toBigInt())
        public val TEN: VarUInt248 = VarUInt248(10.toBigInt())

        //        public val MAX: VarUInt248 = VarUInt248((1.toBigInt() shl 248).minus(1.toBigInt()))
        public val MIN: VarUInt248 = ZERO
    }
}

private object VarUInt248Codec : TlbCodec<VarUInt248> {
    override fun storeTlb(builder: CellBuilder, value: VarUInt248) {
        val len = (value.amount.bitLength + 7) ushr 3
        builder.storeUInt(len, 5)
        builder.storeUInt(value.amount, len * 8)
    }

    override fun loadTlb(slice: CellSlice): VarUInt248 {
        val len = slice.loadUInt(5).toInt()
        val value = slice.loadUInt(len * 8)
        return VarUInt248(value)
    }
}

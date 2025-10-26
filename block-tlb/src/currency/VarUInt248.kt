@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.currency

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.sdk.bigint.*
import org.ton.tlb.TlbCodec

/**
 * Variable-length 248-bit integer. Used for extra currencies.
 *
 * Stored as 5 bits of `len` (0..=31), followed by `len` bytes.
 *
 * @see [org.ton.block.ExtraCurrencyCollection]
 */
public data class VarUInt248(
    public val amount: BigInt
) : Comparable<VarUInt248> {
    init {
        require(amount.sign != -1) { "Amount must be less than zero." }
        require(amount.bitLength <= 248) { "Amount overflow" }
    }

    public constructor(amount: Long) : this(amount.toBigInt())

    public constructor(amount: String, radix: Int) : this(amount.toBigInt(radix))

    override fun compareTo(other: VarUInt248): Int = amount.compareTo(other.amount)

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

package org.ton.kotlin.blockchain.currency

import org.ton.bigint.*
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.cell.CellContext
import org.ton.tlb.TlbCodec

/**
 * Variable-length 248-bit integer.
 */
public class ExtraCoins(
    public val value: BigInt
) : Comparable<ExtraCoins> {
    init {
        require(MIN_VALUE <= value && value <= MAX_VALUE)
    }

    override fun compareTo(other: ExtraCoins): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ExtraCoins
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = "ExtraCoins($value)"

    public companion object {
        private val MIN_VALUE = 0.toBigInt()
        private val MAX_VALUE = (1.toBigInt() shl 248) - 1.toBigInt()

        public val MIN: ExtraCoins = ExtraCoins(MIN_VALUE)
        public val MAX: ExtraCoins = ExtraCoins(MAX_VALUE)

        public fun tlbCodec(): TlbCodec<ExtraCoins> = Tlb
    }

    private object Tlb : TlbCodec<ExtraCoins> {
        override fun loadTlb(slice: CellSlice, context: CellContext): ExtraCoins {
            TODO()
        }

        override fun storeTlb(builder: CellBuilder, value: ExtraCoins, context: CellContext) {
            TODO()
        }
    }
}

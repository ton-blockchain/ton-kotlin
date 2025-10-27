package org.ton.sdk.blockchain.currency

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.cell.CellContext
import org.ton.sdk.bigint.*
import org.ton.tlb.TlbCodec
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName

/**
 * Variable-length 248-bit integer.
 */
public class ExtraCoins(
    @get:JvmName("value")
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

        @JvmField
        public val MIN: ExtraCoins = ExtraCoins(MIN_VALUE)

        @JvmField
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

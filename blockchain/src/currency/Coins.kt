package org.ton.sdk.blockchain.currency

import org.ton.bigint.*
import kotlin.jvm.JvmField

/**
 * Variable-length 120-bit integer. Used for native currencies.
 */
public class Coins(
    public val value: BigInt
) : Comparable<Coins> {
    init {
        require(MIN_VALUE <= value && value <= MAX_VALUE)
    }

    override fun compareTo(other: Coins): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Coins

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String = "Coins(value=$value)"

    public companion object {
        private val MIN_VALUE = 0.toBigInt()
        private val MAX_VALUE = (1.toBigInt() shl 120) - 1.toBigInt()

        @JvmField
        public val ZERO: Coins = Coins(MIN_VALUE)
    }
}

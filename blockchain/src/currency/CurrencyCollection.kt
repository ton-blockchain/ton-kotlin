package org.ton.sdk.blockchain.currency

import kotlin.jvm.JvmField
import kotlin.jvm.JvmName

/**
 * Amounts collection.
 */
public class CurrencyCollection(
    /**
     * Amount in native currency.
     */
    @get:JvmName("coins")
    public val coins: Coins = Coins.ZERO,

    /**
     * Amounts in other currencies.
     */
    @get:JvmName("extra")
    public val extra: ExtraCurrencyCollection = ExtraCurrencyCollection.EMPTY
) {
    public constructor() : this(Coins.ZERO, ExtraCurrencyCollection.EMPTY)
    public constructor(coins: Coins) : this(coins, ExtraCurrencyCollection.EMPTY)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CurrencyCollection) return false
        if (coins != other.coins) return false
        if (extra != other.extra) return false
        return true
    }

    override fun hashCode(): Int {
        var result = coins.hashCode()
        result = 31 * result + extra.hashCode()
        return result
    }

    override fun toString(): String = buildString {
        append("CurrencyCollection(")
        append("coins=$coins")
        if (extra != ExtraCurrencyCollection.EMPTY) {
            append(", extra=$extra")
        }
        append(")")
    }

    public companion object {
        @JvmField
        public val EMPTY: CurrencyCollection = CurrencyCollection(Coins.ZERO, ExtraCurrencyCollection.EMPTY)
    }
}

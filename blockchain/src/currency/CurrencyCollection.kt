package org.ton.sdk.blockchain.currency

/**
 * Amounts collection.
 */
public class CurrencyCollection(
    /**
     * Amount in native currency.
     */
    public val coins: Coins = Coins.ZERO,

    /**
     * Amounts in other currencies.
     */
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
}

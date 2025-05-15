package org.ton.kotlin.block

import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.currency.VarUInt248
import org.ton.kotlin.dict.Dictionary
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbObject
import org.ton.kotlin.tlb.TlbPrettyPrinter
import org.ton.kotlin.tlb.providers.TlbConstructorProvider

/**
 * Amounts collection.
 */
public data class CurrencyCollection(
    /**
     * Amount in native currency.
     */
    val coins: Coins = Coins.ZERO,

    /**
     * Amounts in other currencies.
     */
    val other: ExtraCurrencyCollection = ExtraCurrencyCollection.EMPTY
) : TlbObject {
    public constructor() : this(Coins.ZERO, ExtraCurrencyCollection.EMPTY)

    public constructor(coins: Coins) : this(coins, ExtraCurrencyCollection.EMPTY)

    public constructor(coins: Coins, other: Map<Int, VarUInt248>) : this(coins, ExtraCurrencyCollection(other))

    public constructor(coins: Coins, other: Dictionary<Int, VarUInt248>) : this(coins, ExtraCurrencyCollection(other))

    public constructor(other: Map<Int, VarUInt248>) : this(Coins.ZERO, ExtraCurrencyCollection(other))

    public constructor(other: Dictionary<Int, VarUInt248>) : this(Coins.ZERO, ExtraCurrencyCollection(other))

    public operator fun get(id: Int): VarUInt248? = other[id]

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("currencies") {
        field("coins", coins)
        field("other", other)
    }

    override fun toString(): String =
        if (other.isEmpty()) {
            coins.toString()
        } else buildString {
            append("(")
            append(coins)
            other.forEach { (key, value) ->
                append("+")
                append(value)
                append(".$")
                append(key)
            }
            append(")")
        }

    public companion object : TlbConstructorProvider<CurrencyCollection> by CurrencyCollectionTlbConstructor {
        public val ZERO: CurrencyCollection = CurrencyCollection(Coins.ZERO, ExtraCurrencyCollection.EMPTY)
    }
}

private object CurrencyCollectionTlbConstructor : TlbConstructor<CurrencyCollection>(
    schema = "currencies\$_ coins:Coins other:ExtraCurrencyCollection = CurrencyCollection;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: CurrencyCollection,
        context: CellContext
    ) {
        Coins.storeTlb(builder, value.coins, context)
        ExtraCurrencyCollection.storeTlb(builder, value.other, context)
    }

    override fun loadTlb(
        slice: CellSlice,
        context: CellContext
    ): CurrencyCollection {
        val coins = Coins.loadTlb(slice, context)
        val other = ExtraCurrencyCollection.loadTlb(slice, context)
        return CurrencyCollection(coins, other)
    }
}

package org.ton.kotlin.block

import org.ton.kotlin.cell.Cell
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.currency.VarUInt248
import org.ton.kotlin.dict.Dictionary
import org.ton.kotlin.dict.DictionaryKeyCodec
import org.ton.kotlin.dict.RawDictionary
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider

/**
 * Dictionary with amounts for multiple currencies.
 *
 * @see [CurrencyCollection]
 */
public class ExtraCurrencyCollection : Dictionary<Int, VarUInt248> {
    public constructor() : super(null, DictionaryKeyCodec.INT32, VarUInt248)

    public constructor(map: Map<Int, VarUInt248>, context: CellContext = CellContext.EMPTY) : super(
        map, DictionaryKeyCodec.INT32, VarUInt248, context
    )

    public constructor(cell: Cell?) : super(
        cell, DictionaryKeyCodec.INT32, VarUInt248
    )

    public constructor(rawDictionary: RawDictionary) : super(
        rawDictionary, DictionaryKeyCodec.INT32, VarUInt248
    )

    public constructor(dictionary: Dictionary<Int, VarUInt248>) : super(
        dictionary.dict.root, DictionaryKeyCodec.INT32, VarUInt248
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun toString(): String = this.asSequence().joinToString { (key, value) -> "$key=$value" }

    public companion object : TlbConstructorProvider<ExtraCurrencyCollection> by ExtraCurrencyCollectionTlbConstructor {
        public val EMPTY: ExtraCurrencyCollection = ExtraCurrencyCollection(null)
    }
}

private object ExtraCurrencyCollectionTlbConstructor : TlbConstructor<ExtraCurrencyCollection>(
    schema = "extra_currencies\$_ dict:(HashmapE 32 (VarUInteger 32)) = ExtraCurrencyCollection;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: ExtraCurrencyCollection,
        context: CellContext
    ) {
        builder.storeNullableRef(value.cell)
    }

    override fun loadTlb(
        slice: CellSlice,
        context: CellContext
    ): ExtraCurrencyCollection {
        val cell = slice.loadNullableRef()
        return ExtraCurrencyCollection(cell)
    }
}

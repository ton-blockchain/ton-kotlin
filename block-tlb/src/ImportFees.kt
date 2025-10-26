package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider


@SerialName("import_fees")
public data class ImportFees(
    val feesCollected: Coins,
    val valueImported: CurrencyCollection
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer.type("import_fees") {
            field("fees_collected", feesCollected)
            field("value_imported", valueImported)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<ImportFees> by ImportFeesTlbConstructor
}

private object ImportFeesTlbConstructor : TlbConstructor<ImportFees>(
    schema = "import_fees\$_ fees_collected:Coins value_imported:CurrencyCollection = ImportFees;"
) {
    override fun storeTlb(
        builder: CellBuilder, value: ImportFees
    ) = builder {
        storeTlb(Coins, value.feesCollected)
        storeTlb(CurrencyCollection, value.valueImported)
    }

    override fun loadTlb(
        slice: CellSlice
    ): ImportFees = slice {
        val feesCollected = loadTlb(Coins)
        val valueImported = loadTlb(CurrencyCollection)
        ImportFees(feesCollected, valueImported)
    }
}

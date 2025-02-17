package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.cell.CellContext
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbObject
import org.ton.tlb.TlbPrettyPrinter


@SerialName("import_fees")
public data class ImportFees(
    val feesCollected: Coins = Coins.ZERO,
    val valueImported: CurrencyCollection = CurrencyCollection.ZERO,
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer.type("import_fees") {
            field("fees_collected", feesCollected)
            field("value_imported", valueImported)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbCodec<ImportFees> by ImportFeesTlbCodec {
        public val ZERO: ImportFees = ImportFees()
    }
}

private object ImportFeesTlbCodec : TlbCodec<ImportFees> {
    override fun storeTlb(
        builder: CellBuilder, value: ImportFees, context: CellContext
    ) {
        Coins.storeTlb(builder, value.feesCollected, context)
        CurrencyCollection.storeTlb(builder, value.valueImported, context)
    }

    override fun loadTlb(
        slice: CellSlice,
        context: CellContext
    ): ImportFees {
        val feesCollected = Coins.loadTlb(slice, context)
        val valueImported = CurrencyCollection.loadTlb(slice, context)
        return ImportFees(feesCollected, valueImported)
    }
}

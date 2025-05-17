package org.ton.kotlin.block

import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


public data class ShardFeeCreated(
    val fees: CurrencyCollection,
    val create: CurrencyCollection
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type {
            field("fees", fees)
            field("create", create)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<ShardFeeCreated> by ShardFeeCreatedTlbConstructor
}

private object ShardFeeCreatedTlbConstructor : TlbConstructor<ShardFeeCreated>(
    schema = "_ fees:CurrencyCollection create:CurrencyCollection = ShardFeeCreated;\n"
) {

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: ShardFeeCreated
    ) = cellBuilder {
        storeTlb(CurrencyCollection, value.fees)
        storeTlb(CurrencyCollection, value.create)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): ShardFeeCreated = cellSlice {
        val fees = loadTlb(CurrencyCollection)
        val create = loadTlb(CurrencyCollection)
        ShardFeeCreated(fees, create)
    }
}

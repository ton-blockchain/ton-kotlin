package org.ton.block

import kotlinx.serialization.Serializable
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import org.ton.tlb.providers.TlbConstructorProvider

@Serializable
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

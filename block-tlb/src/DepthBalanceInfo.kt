package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


@SerialName("depth_balance")
public data class DepthBalanceInfo(
    @SerialName("split_depth") val splitDepth: Int,
    val balance: CurrencyCollection
) : TlbObject {
    init {
        require(splitDepth <= 30) { "required: split_depth <= 30, actual: $splitDepth" }
    }

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("depth_balance") {
            field("split_depth", splitDepth)
            field("balance", balance)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<DepthBalanceInfo> by DepthBalanceInfoTlbConstructor
}

private object DepthBalanceInfoTlbConstructor : TlbConstructor<DepthBalanceInfo>(
    schema = "depth_balance\$_ split_depth:(#<= 30) balance:CurrencyCollection = DepthBalanceInfo;"
) {

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: DepthBalanceInfo
    ) = cellBuilder {
        storeUIntLeq(value.splitDepth, 30)
        storeTlb(CurrencyCollection, value.balance)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): DepthBalanceInfo = cellSlice {
        val splitDepth = loadUIntLeq(30).toInt()
        val balance = loadTlb(CurrencyCollection)
        DepthBalanceInfo(splitDepth, balance)
    }
}

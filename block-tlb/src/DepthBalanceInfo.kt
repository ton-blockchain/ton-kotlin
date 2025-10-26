package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.sdk.bigint.toInt
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider


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
        builder: CellBuilder,
        value: DepthBalanceInfo
    ) = builder {
        storeUIntLeq(value.splitDepth, 30)
        storeTlb(CurrencyCollection, value.balance)
    }

    override fun loadTlb(
        slice: CellSlice
    ): DepthBalanceInfo = slice {
        val splitDepth = loadUIntLeq(30).toInt()
        val balance = loadTlb(CurrencyCollection)
        DepthBalanceInfo(splitDepth, balance)
    }
}

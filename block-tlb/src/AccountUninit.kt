package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbPrettyPrinter
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


@SerialName("account_uninit")
public object AccountUninit : AccountState, TlbConstructorProvider<AccountUninit> by AccountUninitTlbConstructor {
    override val status: AccountStatus get() = AccountStatus.UNINIT

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer.type("account_uninit")
    }

    override fun toString(): String = print().toString()
}

private object AccountUninitTlbConstructor : TlbConstructor<AccountUninit>(
    schema = "account_uninit\$00 = AccountState;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: AccountUninit) {
    }

    override fun loadTlb(cellSlice: CellSlice): AccountUninit {
        return AccountUninit
    }
}

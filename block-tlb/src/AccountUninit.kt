package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbPrettyPrinter
import org.ton.tlb.providers.TlbConstructorProvider

@Serializable
@SerialName("account_uninit")
public object AccountUninit : AccountState, TlbConstructorProvider<AccountUninit> by AccountUninitTlbConstructor {
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

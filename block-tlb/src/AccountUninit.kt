package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbPrettyPrinter
import org.ton.tlb.providers.TlbConstructorProvider


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
    override fun storeTlb(builder: CellBuilder, value: AccountUninit) {
    }

    override fun loadTlb(slice: CellSlice): AccountUninit {
        return AccountUninit
    }
}

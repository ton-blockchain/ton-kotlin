package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbPrettyPrinter
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb
import kotlin.jvm.JvmName

@SerialName("account_active")
public class AccountActive(
    @get:JvmName("value")
    public val value: StateInit
) : AccountState {
    override val status: AccountStatus get() = AccountStatus.ACTIVE

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer.type("account_active") {
            value.print(printer)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<AccountActive> by AccountActiveTlbConstructor
}

private object AccountActiveTlbConstructor : TlbConstructor<AccountActive>(
    schema = "account_active\$1 _:StateInit = AccountState;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: AccountActive
    ) = cellBuilder {
        storeTlb(StateInit, value.value)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): AccountActive = cellSlice {
        val init = loadTlb(StateInit)
        AccountActive(init)
    }
}

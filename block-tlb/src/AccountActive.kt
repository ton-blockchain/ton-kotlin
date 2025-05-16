package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbPrettyPrinter
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb
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
    schema = "account_active$1 _:StateInit = AccountState;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: AccountActive
    ) = builder {
        storeTlb(StateInit, value.value)
    }

    override fun loadTlb(
        slice: CellSlice
    ): AccountActive = slice {
        val init = loadTlb(StateInit)
        AccountActive(init)
    }
}

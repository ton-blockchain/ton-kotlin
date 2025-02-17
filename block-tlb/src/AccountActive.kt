package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.account.StateInit
import org.ton.kotlin.cell.CellContext
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName

@JvmInline

@SerialName("account_active")
public value class AccountActive(
    @get:JvmName("value")
    public val value: StateInit
) : AccountState {
    override val status: AccountStatus get() = AccountStatus.ACTIVE

    public companion object : TlbConstructorProvider<AccountActive> by AccountActiveTlbConstructor
}

private object AccountActiveTlbConstructor : TlbConstructor<AccountActive>(
    schema = "account_active\$1 _:StateInit = AccountState;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: AccountActive,
        context: CellContext
    ) {
        StateInit.storeTlb(builder, value.value, context)
    }

    override fun loadTlb(
        slice: CellSlice, context: CellContext
    ): AccountActive {
        val init = StateInit.loadTlb(slice, context)
        return AccountActive(init)
    }
}

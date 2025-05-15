package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import kotlin.jvm.JvmName

@SerialName("account_storage")

@Deprecated("Use fields from Account instead")
public data class AccountStorage(
    @SerialName("last_trans_lt")
    @get:JvmName("lastTransLt")
    val lastTransLt: ULong, // last_trans_lt : uint64

    @get:JvmName("balance")
    val balance: CurrencyCollection, // balance : CurrencyCollection

    @get:JvmName("state")
    val state: AccountState // state : AccountState
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("account_storage") {
        field("last_trans_lt", lastTransLt)
        field("balance", balance)
        field("state", state)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<AccountStorage> by AccountStorageTlbConstructor
}

private object AccountStorageTlbConstructor : TlbConstructor<AccountStorage>(
    schema = "account_storage\$_ last_trans_lt:uint64 " +
            "balance:CurrencyCollection state:AccountState = AccountStorage;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: AccountStorage
    ) = cellBuilder {
        storeUInt64(value.lastTransLt)
        storeTlb(CurrencyCollection, value.balance)
        storeTlb(AccountState, value.state)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): AccountStorage = cellSlice {
        val lastTransLt = loadUInt64()
        val balance = loadTlb(CurrencyCollection)
        val state = loadTlb(AccountState)
        AccountStorage(lastTransLt, balance, state)
    }
}

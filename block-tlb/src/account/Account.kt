@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.account

import org.ton.block.*
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.message.address.MsgAddressInt
import org.ton.tlb.TlbCodec

/**
 * Existing account data.
 *
 * @see [ShardAccount]
 */
public data class Account(
    /**
     * Account address.
     */
    val address: MsgAddressInt,

    /**
     * Storage statistics.
     */
    val storageStat: StorageInfo,

    /**
     * Logical time after the last transaction execution.
     */
    val lastTransLt: Long,

    /**
     * Account balance for all currencies.
     */
    val balance: CurrencyCollection,

    /**
     * Account state.
     */
    val state: AccountState
) {
    @Suppress("DEPRECATION")
    @Deprecated("Use fields lastTransLt, balance, state instead")
    val storage: AccountStorage // storage : AccountStorage
        get() = AccountStorage(lastTransLt.toULong(), balance, state)

    @Deprecated("Use address instead", ReplaceWith("address"))
    val addr: MsgAddressInt
        get() = address

    @Deprecated("use state is AccountActive", ReplaceWith("state is AccountActive"))
    val isActive: Boolean get() = state is AccountActive

    @Deprecated("use state is AccountFrozen", ReplaceWith("state is AccountFrozen"))
    val isFrozen: Boolean get() = state is AccountFrozen

    @Deprecated("use state is AccountUninit", ReplaceWith("state is AccountUninit"))
    val isUninit: Boolean get() = state is AccountUninit

    public companion object : TlbCodec<Account> by AccountTlbCodec
}

public val Account?.balance: CurrencyCollection
    get() = this?.balance ?: CurrencyCollection.Companion.ZERO

public val Account?.accountLastTransLt: Long
    get() = this?.lastTransLt ?: 0

public val Account?.status: AccountStatus
    get() = this?.state?.status ?: AccountStatus.NONEXIST

private object AccountTlbCodec : TlbCodec<Account> {
    override fun storeTlb(
        builder: CellBuilder,
        value: Account,
        context: CellContext
    ) {
        MsgAddressInt.storeTlb(builder, value.address, context)
        StorageInfo.storeTlb(builder, value.storageStat, context)
        builder.storeULong(value.lastTransLt.toULong())
        CurrencyCollection.storeTlb(builder, value.balance, context)
        AccountState.storeTlb(builder, value.state, context)
    }

    override fun loadTlb(
        slice: CellSlice,
        context: CellContext
    ): Account {
        val addr = MsgAddressInt.loadTlb(slice, context)
        val storageStat = StorageInfo.loadTlb(slice, context)
        val lastTransLt = slice.loadULong().toLong()
        val balance = CurrencyCollection.loadTlb(slice, context)
        val state = AccountState.loadTlb(slice, context)
        return Account(addr, storageStat, lastTransLt, balance, state)
    }
}

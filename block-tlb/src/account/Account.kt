package org.ton.kotlin.account

import org.ton.kotlin.block.CurrencyCollection
import org.ton.kotlin.block.MsgAddressInt
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbCodec

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
    /**
     * Deployed account state, if available.
     */
    val stateInit: StateInit? get() = state.stateInit

    public companion object : TlbCodec<Account> by AccountTlbCodec
}

public val Account?.balance: CurrencyCollection
    get() = this?.balance ?: CurrencyCollection.ZERO

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

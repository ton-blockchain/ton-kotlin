package org.ton.kotlin.account

import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbCodec

/**
 * Brief account status.
 *
 * @see [Account]
 */
public enum class AccountStatus {
    /**
     * The account exists but has not yet been deployed.
     */
    UNINIT,

    /**
     * The account exists but has been frozen.
     */
    FROZEN,

    /**
     * The account exists and has been deployed.
     */
    ACTIVE,

    /**
     * The account does not exist.
     */
    NONEXIST;

    public companion object : TlbCodec<AccountStatus> by AccountStatusTlbCodec
}

private object AccountStatusTlbCodec : TlbCodec<AccountStatus> {
    override fun loadTlb(slice: CellSlice, context: CellContext): AccountStatus {
        return AccountStatus.entries[slice.loadUInt(2).toInt()]
    }

    override fun storeTlb(builder: CellBuilder, value: AccountStatus, context: CellContext) {
        builder.storeUInt(value.ordinal.toLong(), 2)
    }
}

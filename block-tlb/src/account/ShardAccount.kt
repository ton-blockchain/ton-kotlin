package org.ton.kotlin.account

import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.CellRef
import org.ton.kotlin.tlb.NullableTlbCodec
import org.ton.kotlin.tlb.TlbCodec

/**
 * Shard accounts entry.
 */
public data class ShardAccount(
    /**
     * Optional reference to account state.
     */
    val account: CellRef<Account?>,

    /**
     * The exact hash of the last transaction.
     */
    val lastTransHash: ByteString,

    /**
     * The exact logical time of the last transaction.
     */
    val lastTransLt: Long
) {
    /**
     * Load account data from a cell.
     */
    public fun loadAccount(context: CellContext = CellContext.EMPTY): Account? {
        return account.load(context)
    }

    public companion object : TlbCodec<ShardAccount> by ShardAccountTlbConstructor
}

private object ShardAccountTlbConstructor : TlbCodec<ShardAccount> {
    private val maybeAccount = NullableTlbCodec(Account)

    override fun storeTlb(
        builder: CellBuilder,
        value: ShardAccount,
        context: CellContext
    ) = builder {
        storeRef(value.account.cell)
        storeBytes(value.lastTransHash.toByteArray())
        storeULong(value.lastTransLt.toULong())
    }

    override fun loadTlb(
        slice: CellSlice,
        context: CellContext
    ): ShardAccount = slice {
        val account = CellRef(loadRef(), maybeAccount)
        val lastTransHash = loadByteString(32)
        val lastTransLt = loadULong().toLong()
        ShardAccount(account, lastTransHash, lastTransLt)
    }
}

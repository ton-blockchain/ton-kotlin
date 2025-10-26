@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.account

import kotlinx.io.bytestring.ByteString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.kotlin.cell.CellContext
import org.ton.tlb.CellRef
import org.ton.tlb.NullableTlbCodec
import org.ton.tlb.TlbCodec

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
     * Load account data from cell.
     */
    public fun loadAccount(context: CellContext = CellContext.EMPTY): Account? {
        return account.load(context)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ShardAccount
        if (lastTransLt != other.lastTransLt) return false
        if (account != other.account) return false
        if (lastTransHash != other.lastTransHash) return false
        return true
    }

    override fun hashCode(): Int {
        var result = lastTransLt.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + lastTransHash.hashCode()
        return result
    }

    public companion object : TlbCodec<ShardAccount> by ShardAccountTlbConstructor
}

private object ShardAccountTlbConstructor : TlbCodec<ShardAccount> {
    private val maybeAccount = NullableTlbCodec(Account)

    override fun storeTlb(
        builder: CellBuilder,
        value: ShardAccount
    ) = builder {
        storeRef(value.account.cell)
        storeBytes(value.lastTransHash.toByteArray())
        storeULong(value.lastTransLt.toULong())
    }

    override fun loadTlb(
        slice: CellSlice
    ): ShardAccount = slice {
        val account = CellRef(loadRef(), maybeAccount)
        val lastTransHash = loadByteString(32)
        val lastTransLt = loadULong().toLong()
        ShardAccount(account, lastTransHash, lastTransLt)
    }
}

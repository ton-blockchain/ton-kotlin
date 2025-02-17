@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.account

import kotlinx.io.bytestring.ByteString
import org.ton.block.CurrencyCollection
import org.ton.block.HashUpdate
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.dict.AugmentedDictionary
import org.ton.kotlin.dict.DictionaryKeyCodec
import org.ton.kotlin.message.address.AddrStd
import org.ton.kotlin.transaction.Transaction
import org.ton.tlb.CellRef
import org.ton.tlb.TlbCodec
import org.ton.tlb.asRef

/**
 * [AccountBlock]s grouped by account id with a total fees as an extra data.
 */
public typealias AccountBlocks = AugmentedDictionary<ByteString, CurrencyCollection, AccountBlock>

/**
 * A group of account transactions.
 *
 * @see [AccountBlocks]
 */
public data class AccountBlock(
    /**
     * Account id.
     */
    val account: ByteString,

    /**
     * Dictionary with fees and account transactions.
     */
    val transactions: AugmentedDictionary<Long, CurrencyCollection, CellRef<Transaction>>,

    /**
     * Account state hashes before and after this block.
     */
    val stateUpdate: CellRef<HashUpdate>
) {
    init {
        require(account.size == 32) { "account size must be 32 bytes" }
        require(transactions.isNotEmpty()) { "At least one transaction must be non-empty" }
    }

    public fun address(workchain: Int): AddrStd = AddrStd(workchain, account)

    public companion object : TlbCodec<AccountBlock> by AccountBlockTlbCodec
}

private object AccountBlockTlbCodec : TlbCodec<AccountBlock> {
    const val TAG = 0x5
    private val LONG_CODEC = DictionaryKeyCodec.long()
    private val REF_TRANSACTION_CODEC = CellRef.tlbCodec(Transaction)

    override fun loadTlb(slice: CellSlice, context: CellContext): AccountBlock {
        val tag = slice.loadUInt(4).toInt()
        require(tag == TAG) { "Invalid acc_trans tag: ${tag.toHexString()}, expected ${TAG.toHexString()}" }

        val account = slice.loadByteString(32)
        val transactions = AugmentedDictionary.loadFromRoot(
            slice,
            LONG_CODEC,
            CurrencyCollection,
            REF_TRANSACTION_CODEC
        )
        val stateUpdate = slice.loadRef().asRef(HashUpdate)

        return AccountBlock(account, transactions, stateUpdate)
    }

    override fun storeTlb(builder: CellBuilder, value: AccountBlock, context: CellContext) {
        val transactions =
            value.transactions.dict.cell?.beginParse() ?: throw IllegalStateException("Invalid transactions dict")
        builder.storeUInt(TAG, 4)
        builder.storeByteString(value.account)
        builder.storeSlice(transactions)
        builder.storeRef(value.stateUpdate.cell)
    }
}

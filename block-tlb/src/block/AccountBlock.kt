package org.ton.block.block

import org.ton.bitstring.BitString
import org.ton.block.currency.CurrencyCollection
import org.ton.block.transaction.HashUpdate
import org.ton.block.transaction.Transaction
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.hashmap.HashmapAug
import org.ton.tlb.*
import org.ton.tlb.providers.TlbCombinatorProvider

public data class AccountBlock(
    val accountAddr: BitString,
    val transactions: HashmapAug<CellRef<Transaction>, CurrencyCollection>,
    val stateUpdate: CellRef<HashUpdate>
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("acc_trans") {
            field("account_addr", accountAddr)
            field("transactions", transactions)
            field("state_update", stateUpdate)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbCombinatorProvider<AccountBlock> by AccountBlockTlbCombinator
}

private object AccountBlockTlbCombinator : TlbCombinator<AccountBlock>(
    AccountBlock::class,
    AccountBlock::class to AccountBlockTlbConstructor
)

private object AccountBlockTlbConstructor : TlbConstructor<AccountBlock>(
    schema = "acc_trans#5 account_addr:bits256" +
            "            transactions:(HashmapAug 64 ^Transaction CurrencyCollection)" +
            "            state_update:^(HASH_UPDATE Account)" +
            "          = AccountBlock"
) {
    val augDictionaryEdge = HashmapAug.tlbCodec(
        64,
        CellRef.tlbCodec(Transaction),
        CurrencyCollection.Tlb
    )

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: AccountBlock
    ) = cellBuilder {
        storeBits(value.accountAddr)
        storeTlb(augDictionaryEdge, value.transactions)
        storeRef(HashUpdate, value.stateUpdate)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): AccountBlock = cellSlice {
        val accountAddr = loadBits(256)
        val transactions = loadTlb(augDictionaryEdge)
        val stateUpdate = loadRef(HashUpdate)
        AccountBlock(accountAddr, transactions, stateUpdate)
    }
}

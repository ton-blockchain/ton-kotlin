package org.ton.kotlin.examples.provider

import kotlinx.io.bytestring.ByteString
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.Message
import org.ton.block.MsgAddressInt
import org.ton.boc.BagOfCells
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.buildCell
import org.ton.kotlin.account.ShardAccount
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.transaction.Transaction
import org.ton.lite.client.LiteClient
import org.ton.lite.client.internal.TransactionId
import org.ton.tlb.CellRef
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbStorer
import org.ton.tlb.storeTlb

interface Provider {
    suspend fun getLastBlockId(): TonNodeBlockIdExt

    suspend fun getAccount(address: MsgAddressInt, blockId: TonNodeBlockIdExt? = null): ShardAccount?

    suspend fun <T : Any> sendMessage(serializer: TlbStorer<T>, message: Message<T>)
    suspend fun getTransactions(
        address: MsgAddressInt,
        fromTxLt: Long? = null,
        fromTxHash: ByteString? = null
    ): List<CellRef<Transaction>>
}

class LiteClientProvider(
    val liteClient: LiteClient
) : Provider {
    override suspend fun getLastBlockId(): TonNodeBlockIdExt {
        return liteClient.getLastBlockId()
    }

    override suspend fun getAccount(
        address: MsgAddressInt,
        blockId: TonNodeBlockIdExt?
    ): ShardAccount? {
        val state = if (blockId == null) {
            liteClient.getAccountState(address)
        } else {
            liteClient.getAccountState(address, blockId)
        }
        val lastTransactionId = state.lastTransactionId
        if (lastTransactionId != null) {
            return ShardAccount(
                state.account,
                ByteString(*lastTransactionId.hash.toByteArray()),
                lastTransactionId.lt
            )
        } else {
            return ShardAccount(
                state.account,
                ByteString(*ByteArray(32)),
                0
            )
        }
    }

    override suspend fun <T : Any> sendMessage(serializer: TlbStorer<T>, message: Message<T>) {
        val cell = buildCell {
            val codec = Message.tlbCodec<T>(object : TlbCodec<T> {
                override fun storeTlb(cellBuilder: CellBuilder, value: T, context: CellContext) =
                    serializer.storeTlb(cellBuilder, value, context)

                override fun loadTlb(cellSlice: CellSlice, context: CellContext): T =
                    throw UnsupportedOperationException()
            })
            storeTlb(codec, message)
        }
        val boc = BagOfCells(cell)
        liteClient.sendMessage(boc)
    }

    override suspend fun getTransactions(
        address: MsgAddressInt,
        fromTxLt: Long?,
        fromTxHash: ByteString?
    ): List<CellRef<Transaction>> {
        if (fromTxLt == null || fromTxHash == null) {
            val account = getAccount(address) ?: return emptyList()
            return getTransactions(address, account.lastTransLt, account.lastTransHash)
        }
        return liteClient.getTransactions(address, TransactionId(fromTxHash.toByteArray(), fromTxLt), 10).map {
            it.transaction
        }
    }
}

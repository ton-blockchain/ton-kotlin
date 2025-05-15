package org.ton.kotlin.lite.client

import org.ton.kotlin.adnl.tonnode.TonNodeBlockIdExt
import org.ton.kotlin.block.MsgAddressInt
import org.ton.kotlin.lite.client.internal.FullAccountState
import org.ton.kotlin.lite.client.internal.TransactionId
import org.ton.kotlin.lite.client.internal.TransactionInfo

public interface LiteClientApi {
    public suspend fun getAccountState(
        accountAddress: MsgAddressInt
    ): FullAccountState

    public suspend fun getAccountState(
        accountAddress: MsgAddressInt,
        blockId: TonNodeBlockIdExt
    ): FullAccountState

    public suspend fun getTransactions(
        accountAddress: MsgAddressInt,
        fromTransactionId: TransactionId,
        count: Int,
    ): List<TransactionInfo>
}

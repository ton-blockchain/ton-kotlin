package org.ton.lite.client

import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.lite.client.internal.FullAccountState
import org.ton.lite.client.internal.TransactionId
import org.ton.lite.client.internal.TransactionInfo

public interface LiteClientApi {
    public suspend fun getAccountState(
        accountAddress: org.ton.kotlin.message.address.MsgAddressInt
    ): FullAccountState

    public suspend fun getAccountState(
        accountAddress: org.ton.kotlin.message.address.MsgAddressInt,
        blockId: TonNodeBlockIdExt
    ): FullAccountState

    public suspend fun getTransactions(
        accountAddress: org.ton.kotlin.message.address.MsgAddressInt,
        fromTransactionId: TransactionId,
        count: Int,
    ): List<TransactionInfo>
}

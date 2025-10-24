package org.ton.kotlin.provider.liteapi

import org.ton.kotlin.provider.liteapi.model.LiteApiMasterchainInfo

public interface LiteApiClient {
    public suspend fun getMasterchainInfo(): LiteApiMasterchainInfo
}

package org.ton.sdk.provider.liteapi

import org.ton.sdk.provider.liteapi.model.LiteApiMasterchainInfo

public interface LiteApiClient {
    public suspend fun getMasterchainInfo(): LiteApiMasterchainInfo
}

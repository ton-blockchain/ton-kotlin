package org.ton.sdk.provider

import org.ton.sdk.blockchain.BlockId

public actual interface Provider {
    public suspend fun getLastMasterchainBlock(): BlockId
}

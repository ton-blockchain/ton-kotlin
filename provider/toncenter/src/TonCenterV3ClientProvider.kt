package org.ton.sdk.provider.toncenter

import kotlinx.io.bytestring.ByteString
import org.ton.sdk.blockchain.BlockId
import org.ton.sdk.provider.Provider
import org.ton.sdk.toncenter.client.TonCenterV3Client
import org.ton.sdk.toncenter.model.TonCenterSendMessageRequest

public class TonCenterV3ClientProvider(
    private val client: TonCenterV3Client
) : Provider {
    override suspend fun getLastMasterchainBlock(): BlockId {
        return client.masterchainInfo().last.blockId
    }

    override suspend fun sendMessage(message: ByteArray) {
        client.message(TonCenterSendMessageRequest(ByteString(message)))
    }
}

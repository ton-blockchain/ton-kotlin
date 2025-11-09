package org.ton.sdk.provider.liteapi

import org.ton.sdk.blockchain.BlockId
import org.ton.sdk.blockchain.ShardId
import org.ton.sdk.crypto.HashBytes
import org.ton.sdk.provider.Provider

public class LiteApiClientProvider(
    private val client: LiteApiClient
) : Provider {
    override suspend fun getLastMasterchainBlock(): BlockId {
        val last = client.getMasterchainInfo().last
        return BlockId(
            shardId = ShardId(last.workchain, last.shard.toULong()),
            seqno = last.seqno,
            rootHash = HashBytes(last.rootHash),
            fileHash = HashBytes(last.fileHash)
        )
    }

    override suspend fun sendMessage(message: ByteArray) {
        client.sendMessage(message)
    }
}

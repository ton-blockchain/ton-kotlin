package org.ton.kotlin.blockchain

import kotlinx.io.bytestring.ByteString

public class BlockId(
    public val shardId: ShardId,
    public val seqno: Int,
    public val rootHash: ByteString,
    public val fileHash: ByteString
)

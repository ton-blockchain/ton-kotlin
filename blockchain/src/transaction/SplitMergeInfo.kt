package org.ton.sdk.blockchain.transaction

import org.ton.sdk.crypto.HashBytes

@Deprecated("Not implemented in TON Blockchain")
public class SplitMergeInfo(
    public val currentShardPrefixLength: Int,
    public val accountSplitDepth: Int,
    public val thisAddress: HashBytes,
    public val siblingAddress: HashBytes
)

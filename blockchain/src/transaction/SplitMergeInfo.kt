package org.ton.sdk.blockchain.transaction

import org.ton.sdk.crypto.HashBytes
import kotlin.jvm.JvmName

@Deprecated("Not implemented in TON Blockchain")
public class SplitMergeInfo(
    @get:JvmName("currentShardPrefixLength")
    public val currentShardPrefixLength: Int,
    @get:JvmName("accountSplitDepth")
    public val accountSplitDepth: Int,
    @get:JvmName("thisAddress")
    public val thisAddress: HashBytes,
    @get:JvmName("siblingAddress")
    public val siblingAddress: HashBytes
)

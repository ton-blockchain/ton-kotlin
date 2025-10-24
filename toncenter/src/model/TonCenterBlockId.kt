package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable

@Serializable
public class TonCenterBlockId(
    public val workchain: Int,
    public val shard: Long,
    public val seqno: Int,
)

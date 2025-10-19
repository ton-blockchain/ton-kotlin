package org.ton.kotlin.provider.toncenter.model

import kotlinx.serialization.Serializable

@Serializable
public data class TonCenterBlockId(
    public val workchain: Int,
    public val shard: Long,
    public val seqno: Int,
)

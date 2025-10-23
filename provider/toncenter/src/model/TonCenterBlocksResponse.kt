package org.ton.kotlin.provider.toncenter.model

import kotlinx.serialization.Serializable

@Serializable
public data class TonCenterBlocksResponse(
    public val blocks: List<TonCenterBlock>
)

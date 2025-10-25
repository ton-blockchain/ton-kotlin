package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable

@Serializable
public class TonCenterBlocksResponse(
    public val blocks: List<TonCenterBlock>
)

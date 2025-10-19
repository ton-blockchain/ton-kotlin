package org.ton.kotlin.provider.toncenter.model

import kotlinx.serialization.Serializable

@Serializable
public data class TonCenterMasterchainInfo(
    public val first: TonCenterBlock,
    public val last: TonCenterBlock
)

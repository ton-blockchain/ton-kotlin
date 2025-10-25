package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable

@Serializable
public class TonCenterMasterchainInfo(
    public val first: TonCenterBlock,
    public val last: TonCenterBlock
)

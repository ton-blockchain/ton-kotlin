package org.ton.kotlin.provider.toncenter.model

import kotlinx.serialization.Serializable

@Serializable
public data class TonCenterAddressMetadata(
    val isIndexed: Boolean,
    val tokenInfo: TonCenterTokenInfo
)

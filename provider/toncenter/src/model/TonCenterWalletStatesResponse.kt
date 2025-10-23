package org.ton.kotlin.provider.toncenter.model

import kotlinx.serialization.Serializable

@Serializable
public data class TonCenterWalletStatesResponse(
    public val wallets: List<TonCenterWalletState>,
    public val addressBook: TonCenterAddressBook,
    public val metadata: TonCenterMetadata,
)

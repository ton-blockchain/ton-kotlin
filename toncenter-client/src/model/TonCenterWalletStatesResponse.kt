package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable

@Serializable
public class TonCenterWalletStatesResponse(
    public val wallets: List<TonCenterWalletState>,
    public val addressBook: TonCenterAddressBook,
    public val metadata: TonCenterMetadata,
)

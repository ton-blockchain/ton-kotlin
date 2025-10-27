package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmName

@Serializable
public class TonCenterWalletStatesResponse(
    @get:JvmName("wallets") public val wallets: List<TonCenterWalletState>,
    @get:JvmName("addressBook") public val addressBook: TonCenterAddressBook,
    @get:JvmName("metadata") public val metadata: TonCenterMetadata,
)

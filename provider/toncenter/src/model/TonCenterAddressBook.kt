package org.ton.kotlin.provider.toncenter.model

import kotlinx.serialization.Serializable

@Serializable
public data class TonCenterAddressBookRow(
    val userFriendly: String,
    val domain: String?
)

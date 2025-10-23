package org.ton.kotlin.provider.toncenter.model

import kotlinx.serialization.Serializable
import org.ton.kotlin.blockchain.message.address.AddressStd
import org.ton.kotlin.provider.toncenter.internal.serializers.AddressStdAsBase64Serializer

@Serializable
public data class TonCenterTokenInfo(
    val valid: Boolean,
    val type: String,
    val name: String,
    val symbol: String,
    val description: String,
    val image: String,
    val extra: Map<@Serializable(AddressStdAsBase64Serializer::class) AddressStd, String>
)

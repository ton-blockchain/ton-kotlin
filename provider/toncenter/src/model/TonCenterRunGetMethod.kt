package org.ton.kotlin.provider.toncenter.model

import kotlinx.serialization.Serializable
import org.ton.kotlin.blockchain.message.address.AddressStd
import org.ton.kotlin.provider.toncenter.internal.serializers.AddressStdAsBase64Serializer

@Serializable
public data class TonCenterRunGetMethodRequest(
    @Serializable(with = AddressStdAsBase64Serializer::class)
    val address: AddressStd,
    val method: String,
    val stack: List<TonCenterStackEntry<*>>
) {
    public constructor(address: AddressStd, method: String) : this(address, method, listOf())
}

@Serializable
public data class TonCenterRunGetMethodResult(
    val gasUsed: Long,
    val exitCode: Int,
    val stack: List<TonCenterStackEntry<*>>
)

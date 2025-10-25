package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable
import org.ton.sdk.blockchain.address.AddressStd
import org.ton.sdk.toncenter.internal.serializers.AddressStdAsBase64Serializer

@Serializable
public class TonCenterRunGetMethodRequest(
    @Serializable(with = AddressStdAsBase64Serializer::class)
    public val address: AddressStd,
    public val method: String,
    public val stack: List<TonCenterStackEntry<*>>
) {
    public constructor(address: AddressStd, method: String) : this(address, method, listOf())
}

@Serializable
public class TonCenterRunGetMethodResult(
    public val gasUsed: Long,
    public val exitCode: Int,
    public val stack: List<TonCenterStackEntry<*>>
)

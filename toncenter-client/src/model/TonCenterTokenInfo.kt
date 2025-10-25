package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable
import org.ton.sdk.blockchain.address.AddressStd
import org.ton.sdk.toncenter.internal.serializers.AddressStdAsBase64Serializer

@Serializable
public class TonCenterTokenInfo(
    public val valid: Boolean,
    public val type: String,
    public val name: String,
    public val symbol: String,
    public val description: String,
    public val image: String,
    public val extra: Map<@Serializable(AddressStdAsBase64Serializer::class) AddressStd, String>
)

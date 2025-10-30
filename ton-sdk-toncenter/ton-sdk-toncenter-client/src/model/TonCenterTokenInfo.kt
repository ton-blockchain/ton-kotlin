package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable
import org.ton.sdk.blockchain.address.AddressStd
import org.ton.sdk.toncenter.internal.serializers.AddressStdAsBase64Serializer
import kotlin.jvm.JvmName

@Serializable
public class TonCenterTokenInfo(
    @get:JvmName("valid")
    public val valid: Boolean,
    @get:JvmName("type")
    public val type: String,
    @get:JvmName("name")
    public val name: String,
    @get:JvmName("symbol")
    public val symbol: String,
    @get:JvmName("description")
    public val description: String,
    @get:JvmName("image")
    public val image: String,
    @get:JvmName("extra")
    public val extra: Map<@Serializable(AddressStdAsBase64Serializer::class) AddressStd, String>
)

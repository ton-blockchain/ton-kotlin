package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable
import org.ton.sdk.blockchain.address.AddressStd
import org.ton.sdk.toncenter.internal.serializers.AddressStdAsBase64Serializer
import kotlin.jvm.JvmName

@Serializable
public class TonCenterRunGetMethodRequest(
    @Serializable(with = AddressStdAsBase64Serializer::class)
    @get:JvmName("address")
    public val address: AddressStd,
    @get:JvmName("method")
    public val method: String,
    @get:JvmName("stack")
    public val stack: List<TonCenterStackEntry<*>>
) {
    public constructor(address: AddressStd, method: String) : this(address, method, listOf())
}

@Serializable
public class TonCenterRunGetMethodResult(
    @get:JvmName("gasUsed")
    public val gasUsed: Long,
    @get:JvmName("exitCode")
    public val exitCode: Int,
    @get:JvmName("stack")
    public val stack: List<TonCenterStackEntry<*>>
)

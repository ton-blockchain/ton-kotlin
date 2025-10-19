package org.ton.kotlin.provider.toncenter.model

import kotlinx.serialization.Serializable

@Serializable
public data class TonCenterRunGetMethodRequest(
    val address: String,
    val method: String,
    val stack: List<TonCenterStackEntry<*>>
)

@Serializable
public data class TonCenterRunGetMethodResult(
    val gasUsed: Long,
    val exitCode: Int,
    val stack: List<TonCenterStackEntry<*>>
)

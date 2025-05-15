package org.ton.kotlin.api.liteclient.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.api.dht.config.DhtConfigGlobal
import org.ton.kotlin.api.liteserver.LiteServerDesc
import org.ton.kotlin.api.validator.config.ValidatorConfigGlobal

@SerialName("liteclient.config.global")
@Serializable
public data class LiteClientConfigGlobal(
    val dht: DhtConfigGlobal = DhtConfigGlobal(),
    @SerialName("liteservers")
    val liteServers: Collection<LiteServerDesc>,
    val validator: ValidatorConfigGlobal = ValidatorConfigGlobal()
)

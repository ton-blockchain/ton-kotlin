package org.ton.kotlin.api.liteserver.config

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@SerialName("liteserver.config.random.local")
@Polymorphic
@Serializable
@JsonClassDiscriminator("@type")
public data class LiteServerConfigRandomLocal(
    val port: Int
) : LiteServerConfigLocal

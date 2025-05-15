package org.ton.kotlin.api.liteserver.config

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@Polymorphic
@JsonClassDiscriminator("@type")
public sealed interface LiteServerConfigLocal

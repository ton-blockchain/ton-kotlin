@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.api.id.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.api.pk.PrivateKey

@Serializable
@SerialName("id.config.local")
@JsonClassDiscriminator("@type")
public data class IdConfigLocal(
    val id: PrivateKey
)

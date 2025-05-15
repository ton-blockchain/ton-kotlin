@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.api.config

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.api.control.ControlConfigLocal
import org.ton.kotlin.api.dht.config.DhtConfigLocal
import org.ton.kotlin.api.id.config.IdConfigLocal
import org.ton.kotlin.api.liteserver.config.LiteServerConfigLocal
import org.ton.kotlin.api.validator.config.ValidatorConfigLocal

@Serializable
@Polymorphic
@SerialName("config.local")
@JsonClassDiscriminator("@type")
public data class ConfigLocal(
    @SerialName("local_ids")
    val localIds: Collection<IdConfigLocal>,
    val dht: Collection<DhtConfigLocal>,
    val validators: Collection<ValidatorConfigLocal>,
    @SerialName("liteservers")
    val liteServers: Collection<LiteServerConfigLocal>,
    val control: Collection<ControlConfigLocal>
)

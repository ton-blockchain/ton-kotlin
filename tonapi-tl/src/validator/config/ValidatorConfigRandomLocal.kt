package org.ton.kotlin.api.validator.config

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.api.adnl.AdnlAddressList

@SerialName("validator.config.random.local")
@Polymorphic
@Serializable
@JsonClassDiscriminator("@type")
public data class ValidatorConfigRandomLocal(
    @SerialName("addr_list")
    val addrList: AdnlAddressList
) : ValidatorConfigLocal

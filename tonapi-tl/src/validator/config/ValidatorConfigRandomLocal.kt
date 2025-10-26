package org.ton.api.validator.config

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.adnl.AdnlAddressList

@SerialName("validator.config.random.local")
@Polymorphic
@Serializable
public data class ValidatorConfigRandomLocal(
    @SerialName("addr_list")
    val addrList: AdnlAddressList
) : ValidatorConfigLocal

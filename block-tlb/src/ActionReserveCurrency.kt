package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action_reserve_currency")
public data class ActionReserveCurrency(
    val mode: Int,
    val currency: CurrencyCollection
) : OutAction

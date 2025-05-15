@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.api.validator.config

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@Polymorphic
@JsonClassDiscriminator("@type")
public sealed interface ValidatorConfigLocal

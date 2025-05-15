@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.block

import kotlinx.serialization.json.JsonClassDiscriminator


@JsonClassDiscriminator("@type")
public sealed interface LibRef

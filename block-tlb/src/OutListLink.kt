package org.ton.kotlin.block

import kotlinx.serialization.SerialName


@SerialName("out_list")
public data class OutListLink(
    val prev: OutList,
    val action: OutAction
) : OutList

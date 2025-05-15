package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.Cell


@SerialName("out_list_node")
public data class OutListNode(
    val prev: Cell,
    val action: OutAction
)

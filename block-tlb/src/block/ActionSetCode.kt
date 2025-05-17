package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.Cell


@SerialName("action_set_code")
public data class ActionSetCode(
    val newCode: Cell
) : OutAction

package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.Cell


@SerialName("action_send_msg")
public data class ActionSendMsg(
    val mode: Int,
    val outMsg: MessageRelaxed<Cell>
) : OutAction

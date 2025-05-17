package org.ton.kotlin.block

import kotlinx.serialization.SerialName


@SerialName("action_change_library")
public data class ActionChangeLibrary(
    val mode: Int,
    @Suppress("SpellCheckingInspection")
    val libref: LibRef
) : OutAction {
    override fun toString(): String = buildString {
        append("(action_change_library\n")
        append("mode:")
        append(mode)
        append(" libref:")
        append(libref)
        append(")")
    }
}

package org.ton.kotlin.overlay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.TlConstructorId

@Serializable
@SerialName("overlay.message")
@TlConstructorId(0x75252420)
data class OverlayMessage(
    val overlay: OverlayIdShort
)

package org.ton.kotlin.overlay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.overlay.broadcast.BroadcastIdShort
import org.ton.kotlin.overlay.broadcast.BroadcastInfoList
import org.ton.kotlin.tl.TlConstructorId

@Serializable
sealed interface OverlayFunction {

    @Serializable
    @SerialName("overlay.query")
    @TlConstructorId(0xccfd8443)
    data class Query(
        val overlay: OverlayIdShort,
    ) : OverlayFunction {
        constructor(overlay: OverlayIdFull) : this(overlay.shortId)
    }

    @Serializable
    @SerialName("overlay.ping")
    @TlConstructorId(0x690cb481)
    data object Ping : OverlayFunction

    @Serializable
    @SerialName("overlay.pong")
    @TlConstructorId(0x67700804)
    data object Pong

    @Serializable
    @SerialName("overlay.getRandomPeers")
    @TlConstructorId(0x48ee64ab)
    data class GetRandomPeers(
        val peers: OverlayNodeInfoList,
    ) : OverlayFunction

    @Serializable
    @SerialName("overlay.getBroadcast")
    @TlConstructorId(0x2d35f2a0)
    data class GetBroadcast(
        val hash: BroadcastIdShort
    ) : OverlayFunction

    @Serializable
    @SerialName("overlay.getBroadcastList")
    @TlConstructorId(0x421c283a)
    data class GetBroadcastList(
        val list: BroadcastInfoList
    )
}

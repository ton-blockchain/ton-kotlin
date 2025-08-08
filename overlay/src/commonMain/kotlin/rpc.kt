package org.ton.kotlin.overlay

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.overlay.broadcast.BroadcastIdShort
import org.ton.kotlin.overlay.broadcast.BroadcastInfo
import org.ton.kotlin.overlay.broadcast.BroadcastInfoList
import org.ton.kotlin.tl.TlConstructorId

@Serializable
sealed interface OverlayFunction<Answer> {
    val answerSerializer: KSerializer<Answer>

    @Serializable
    @SerialName("overlay.query")
    @TlConstructorId(0xccfd8443)
    data class Query<Answer>(
        val overlay: OverlayIdShort,
        val query: OverlayFunction<Answer>,
    ) : OverlayFunction<Answer> {
        override val answerSerializer: KSerializer<Answer>
            get() = query.answerSerializer
    }

    @Serializable
    @SerialName("overlay.getRandomPeers")
    @TlConstructorId(0x48ee64ab)
    data class GetRandomPeers(
        val peers: OverlayNodeInfoList,
    ) : OverlayFunction<OverlayNodeInfoList> {
        override val answerSerializer: KSerializer<OverlayNodeInfoList>
            get() = OverlayNodeInfoList.serializer()
    }

    @Serializable
    @SerialName("overlay.getBroadcast")
    @TlConstructorId(0x2d35f2a0)
    data class GetBroadcast(
        val hash: BroadcastIdShort
    ) : OverlayFunction<BroadcastInfo> {
        override val answerSerializer: KSerializer<BroadcastInfo>
            get() = BroadcastInfo.serializer()
    }

    @Serializable
    @SerialName("overlay.getBroadcastList")
    @TlConstructorId(0x421c283a)
    data class GetBroadcastList(
        val list: BroadcastInfoList
    )
}

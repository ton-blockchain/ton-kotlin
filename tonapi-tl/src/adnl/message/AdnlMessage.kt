@file:Suppress("OPT_IN_USAGE")

package org.ton.api.adnl.message

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.tl.TlCodec
import org.ton.tl.TlCombinator
import org.ton.tl.TlObject

@Polymorphic
@Serializable
@JsonClassDiscriminator("@type")
public sealed interface AdnlMessage : TlObject<AdnlMessage> {
    override fun tlCodec(): TlCodec<out AdnlMessage> = Companion

    public companion object : TlCombinator<AdnlMessage>(
        AdnlMessage::class,
        AdnlMessageQuery::class to AdnlMessageQuery,
        AdnlMessageAnswer::class to AdnlMessageAnswer,
        AdnlMessageCreateChannel::class to AdnlMessageCreateChannel,
        AdnlMessageConfirmChannel::class to AdnlMessageConfirmChannel,
        AdnlMessageCustom::class to AdnlMessageCustom,
        AdnlMessageNop::class to AdnlMessageNop,
        AdnlMessageReinit::class to AdnlMessageReinit,
        AdnlMessagePart::class to AdnlMessagePart.tlConstructor(),
    ) {
        public fun sizeOf(message: AdnlMessage): Int = Int.SIZE_BYTES + when (message) {
            is AdnlMessageQuery -> AdnlMessageQuery.sizeOf(message)
            is AdnlMessageAnswer -> AdnlMessageAnswer.sizeOf(message)
            is AdnlMessageCreateChannel -> AdnlMessageCreateChannel.SIZE_BYTES
            is AdnlMessageConfirmChannel -> AdnlMessageConfirmChannel.SIZE_BYTES
            is AdnlMessageCustom -> AdnlMessageCustom.sizeOf(message)
            is AdnlMessageNop -> AdnlMessageNop.SIZE_BYTES
            is AdnlMessageReinit -> AdnlMessageReinit.SIZE_BYTES
            is AdnlMessagePart -> AdnlMessagePart.sizeOf(message)
        }
    }
}

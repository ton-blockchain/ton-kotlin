package org.ton.kotlin.lite.api.liteserver

import org.ton.kotlin.adnl.tonnode.TonNodeBlockIdExt
import org.ton.kotlin.tl.TlCodec
import org.ton.kotlin.tl.TlCombinator

public sealed interface LiteServerBlockLink {
    public val toKeyBlock: Boolean
    public val from: TonNodeBlockIdExt
    public val to: TonNodeBlockIdExt

    public companion object : TlCodec<LiteServerBlockLink> by LiteServerBlockLinkTlCombinator
}

private object LiteServerBlockLinkTlCombinator : TlCombinator<LiteServerBlockLink>(
    LiteServerBlockLink::class,
    LiteServerBlockLinkBack::class to LiteServerBlockLinkBack.tlConstructor(),
    LiteServerBlockLinkForward::class to LiteServerBlockLinkForward.tlConstructor()
)

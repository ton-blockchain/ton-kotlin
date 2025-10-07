package org.ton.lite.api.liteserver

import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.tl.TlCodec
import org.ton.tl.TlCombinator

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

package org.ton.kotlin.block

import org.ton.kotlin.tlb.TlbCombinator
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider

public sealed interface Protocol {
    public companion object : TlbCombinatorProvider<Protocol> by ProtocolTlbCombinator
}

private object ProtocolTlbCombinator : TlbCombinator<Protocol>(
    Protocol::class,
    ProtoHttp::class to ProtoHttp.tlbConstructor(),
)

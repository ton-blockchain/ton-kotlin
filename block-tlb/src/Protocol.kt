package org.ton.block

import org.ton.tlb.TlbCombinator
import org.ton.tlb.providers.TlbCombinatorProvider

public sealed interface Protocol {
    public companion object : TlbCombinatorProvider<Protocol> by ProtocolTlbCombinator
}

private object ProtocolTlbCombinator : TlbCombinator<Protocol>(
    Protocol::class,
    ProtoHttp::class to ProtoHttp.tlbConstructor(),
)

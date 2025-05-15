package org.ton.kotlin.block

import org.ton.kotlin.tlb.providers.TlbCombinatorProvider

public sealed interface ProtoList : Iterable<Protocol> {
    public companion object : TlbCombinatorProvider<ProtoList> by ProtoListTlbCombinator
}

private object ProtoListTlbCombinator : org.ton.kotlin.tlb.TlbCombinator<ProtoList>(
    ProtoList::class,
    ProtoListNil::class to ProtoListNil.tlbConstructor(),
    ProtoListNext::class to ProtoListNext.tlbConstructor(),
)

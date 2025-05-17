package org.ton.kotlin.block

import org.ton.kotlin.cell.CellSizeable
import org.ton.kotlin.tlb.TlbCombinator
import org.ton.kotlin.tlb.TlbObject
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider


public sealed interface MsgAddress : TlbObject, CellSizeable {
    public companion object : TlbCombinatorProvider<MsgAddress> by MsgAddressTlbCombinator
}

private object MsgAddressTlbCombinator : TlbCombinator<MsgAddress>(
    MsgAddress::class,
    MsgAddressInt::class to MsgAddressInt.tlbCodec(),
    MsgAddressExt::class to MsgAddressExt.tlbCodec(),
)

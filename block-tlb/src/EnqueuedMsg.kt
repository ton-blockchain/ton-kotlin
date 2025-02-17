package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.CellRef
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadRef
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeRef


public data class EnqueuedMsg(
    @SerialName("enqueued_lt") val enqueuedLt: ULong,
    @SerialName("out_msg") val outMsg: CellRef<MsgEnvelope>
) {
    public companion object : TlbConstructorProvider<EnqueuedMsg> by EnqueuedMsgTlbConstructor
}

private object EnqueuedMsgTlbConstructor : TlbConstructor<EnqueuedMsg>(
    schema = "_ enqueued_lt:uint64 out_msg:^MsgEnvelope = EnqueuedMsg;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: EnqueuedMsg
    ) = cellBuilder {
        storeUInt64(value.enqueuedLt)
        storeRef(MsgEnvelope, value.outMsg)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): EnqueuedMsg = cellSlice {
        val enqueuedLt = loadUInt64()
        val outMsg = loadRef(MsgEnvelope)
        EnqueuedMsg(enqueuedLt, outMsg)
    }
}

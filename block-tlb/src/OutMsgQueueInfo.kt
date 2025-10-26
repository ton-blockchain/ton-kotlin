package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.hashmap.HashMapE
import org.ton.hashmap.HashmapAugE
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor
import org.ton.tlb.constructor.tlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider


public data class OutMsgQueueInfo(
    @SerialName("out_queue") val outQueue: HashmapAugE<EnqueuedMsg, ULong>, // out_queue : OutMsgQueue
    @SerialName("proc_info") val procInfo: HashMapE<ProcessedUpto>, // proc_info : ProcessedInfo
    @SerialName("ihr_pending") val ihrPending: HashMapE<IhrPendingSince> // ihr_pending : IhrPendingInfo
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer.type("out_msg_queue_info") {
            field("out_queue", outQueue)
            field("proc_info", procInfo)
            field("ihr_pending", ihrPending)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<OutMsgQueueInfo> by OutMsgQueueInfoTlbConstructor
}

// _ (HashmapAugE 352 EnqueuedMsg uint64) = OutMsgQueue;
// _ (HashmapE 96 ProcessedUpto) = ProcessedInfo;
// _ (HashmapE 320 IhrPendingSince) = IhrPendingInfo;
private object OutMsgQueueInfoTlbConstructor : TlbConstructor<OutMsgQueueInfo>(
    schema = "_ out_queue:OutMsgQueue proc_info:ProcessedInfo ihr_pending:IhrPendingInfo = OutMsgQueueInfo;"
) {
    val outQueue = HashmapAugE.tlbCodec(352, EnqueuedMsg, ULong.tlbConstructor())
    val procInfo = HashMapE.tlbCodec(96, ProcessedUpto)
    val ihrPending = HashMapE.tlbCodec(320, IhrPendingSince)

    override fun storeTlb(
        builder: CellBuilder,
        value: OutMsgQueueInfo
    ) = builder {
        storeTlb(outQueue, value.outQueue)
        storeTlb(procInfo, value.procInfo)
        storeTlb(ihrPending, value.ihrPending)
    }

    override fun loadTlb(
        slice: CellSlice
    ): OutMsgQueueInfo = slice {
        val outQueue = loadTlb(outQueue)
        val procInfo = loadTlb(procInfo)
        val ihrPending = loadTlb(ihrPending)
        OutMsgQueueInfo(outQueue, procInfo, ihrPending)
    }
}

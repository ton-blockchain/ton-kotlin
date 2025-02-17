package org.ton.block

import org.ton.block.message.output.OutMsgQueueExtra
import org.ton.block.message.output.OutMsgQueueKey
import org.ton.block.message.output.ProcessedInfoKey
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.dict.AugmentedDictionary
import org.ton.kotlin.dict.Dictionary
import org.ton.tlb.NullableTlbCodec
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider

public typealias OutMsgQueue = AugmentedDictionary<OutMsgQueueKey, Long, EnqueuedMsg>

public typealias ProcessedInfo = Dictionary<ProcessedInfoKey, ProcessedUpto>

public data class OutMsgQueueInfo(
    val outQueue: OutMsgQueue,
    val procInfo: ProcessedInfo,
    val extra: OutMsgQueueExtra?
) {
    public companion object : TlbConstructorProvider<OutMsgQueueInfo> by OutMsgQueueInfoTlbConstructor
}

private object OutMsgQueueInfoTlbConstructor : TlbConstructor<OutMsgQueueInfo>(
    schema = "_ out_queue:OutMsgQueue proc_info:ProcessedInfo ihr_pending:IhrPendingInfo = OutMsgQueueInfo;"
) {
    val LONG_CODEC = TlbCodec.long(64)
    val OUT_MSG_QUEUE_CODEC = AugmentedDictionary.tlbCodec(
        OutMsgQueueKey, LONG_CODEC, TlbCodec.pair(
            LONG_CODEC,
            EnqueuedMsg
        )
    )
    val PROCESSED_INFO_CODEC = Dictionary.tlbCodec(
        ProcessedInfoKey,
        ProcessedUpto
    )
    val OUT_MSG_QUEUE_EXTRA_CODEC = NullableTlbCodec(OutMsgQueueExtra)

    override fun storeTlb(
        builder: CellBuilder,
        value: OutMsgQueueInfo,
        context: CellContext
    ) {
        OUT_MSG_QUEUE_CODEC.storeTlb(builder, value.outQueue, context)
        PROCESSED_INFO_CODEC.storeTlb(builder, value.procInfo, context)
        OUT_MSG_QUEUE_EXTRA_CODEC.storeTlb(builder, value.extra, context)
    }

    override fun loadTlb(
        slice: CellSlice,
        context: CellContext
    ): OutMsgQueueInfo {
        val outQueue = OUT_MSG_QUEUE_CODEC.loadTlb(slice, context)
        val procInfo = PROCESSED_INFO_CODEC.loadTlb(slice, context)
        val extra = OUT_MSG_QUEUE_EXTRA_CODEC.loadTlb(slice, context)
        return OutMsgQueueInfo(outQueue, procInfo, extra)
    }
}

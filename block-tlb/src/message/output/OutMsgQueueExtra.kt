package org.ton.block.message.output

import org.ton.bitstring.BitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.dict.AugmentedDictionary
import org.ton.kotlin.dict.DictionaryKeyCodec
import org.ton.tlb.TlbCodec

public typealias DispatchQueue = AugmentedDictionary<BitString, Long, AccountDispatchQueue>

public data class OutMsgQueueExtra(
    val dispatchQueue: DispatchQueue,
    val outQueueSize: Long?,
) {
    public companion object : TlbCodec<OutMsgQueueExtra> {
        private val TAG = 0x0
        private val LONG_CODEC = TlbCodec.long(64)
        private val DISPATCH_QUEUE_CODEC = AugmentedDictionary.tlbCodec(
            DictionaryKeyCodec.BITS256,
            LONG_CODEC,
            TlbCodec.pair(LONG_CODEC, AccountDispatchQueue)
        )

        override fun loadTlb(slice: CellSlice, context: CellContext): OutMsgQueueExtra {
            val tag = slice.loadUInt(4).toInt()
            require(tag == TAG) { "Invalid OutMsgQueueExtra tag: $tag" }

            val dispatchQueue = DISPATCH_QUEUE_CODEC.loadTlb(slice, context)
            val outQueueSize = if (slice.loadBoolean()) slice.loadULong(48).toLong() else null

            return OutMsgQueueExtra(dispatchQueue, outQueueSize)
        }

        override fun storeTlb(builder: CellBuilder, value: OutMsgQueueExtra, context: CellContext) {
            builder.storeUInt(TAG, 4)
            DISPATCH_QUEUE_CODEC.storeTlb(builder, value.dispatchQueue, context)
            if (value.outQueueSize != null) {
                builder.storeULong((1L shl 48 or value.outQueueSize).toULong(), 49)
            } else {
                builder.storeBoolean(false)
            }
        }
    }
}
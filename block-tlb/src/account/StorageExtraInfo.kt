package org.ton.kotlin.account

import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbCodec

public data class StorageExtraInfo(
    val dictHash: ByteString
) {
    public companion object : TlbCodec<StorageExtraInfo?> by StorageExtraInfoTlbCodec
}

private object StorageExtraInfoTlbCodec : TlbCodec<StorageExtraInfo?> {
    override fun loadTlb(slice: CellSlice, context: CellContext): StorageExtraInfo? {
        val tag = slice.loadUInt(3).toInt()
        when (tag) {
            0b000 -> return null
            0b001 -> {
                val dictHash = slice.loadByteString(32)
                return StorageExtraInfo(dictHash)
            }

            else -> {
                throw IllegalArgumentException("Invalid tag: $tag")
            }
        }
    }

    override fun storeTlb(builder: CellBuilder, value: StorageExtraInfo?, context: CellContext) {
        if (value == null) {
            builder.storeUInt(0b000, 3)
        } else {
            builder.storeUInt(0b001, 3)
            builder.storeByteString(value.dictHash)
        }
    }
}

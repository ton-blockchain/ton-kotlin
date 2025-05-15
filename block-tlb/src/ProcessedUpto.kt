package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbObject
import org.ton.kotlin.tlb.TlbPrettyPrinter
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


@SerialName("processed_upto")
public data class ProcessedUpto(
    @SerialName("last_msg_lt") val lastMsgLt: ULong,
    @SerialName("last_msg_hash") val lastMsgHash: BitString
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("processed_upto") {
        field("last_msg_lt", lastMsgLt)
        field("last_msg_hash", lastMsgHash)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<ProcessedUpto> by ProcessedUptoTlbConstructor
}

private object ProcessedUptoTlbConstructor : TlbConstructor<ProcessedUpto>(
    schema = "processed_upto\$_ last_msg_lt:uint64 last_msg_hash:bits256 = ProcessedUpto;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: ProcessedUpto
    ) = cellBuilder {
        storeUInt64(value.lastMsgLt)
        storeBits(value.lastMsgHash)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): ProcessedUpto = cellSlice {
        val lastMsgLt = loadUInt64()
        val lastMsgHash = loadBits(256)
        ProcessedUpto(lastMsgLt, lastMsgHash)
    }
}

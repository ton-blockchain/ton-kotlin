package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.bitstring.BitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbObject
import org.ton.tlb.TlbPrettyPrinter
import org.ton.tlb.providers.TlbConstructorProvider


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
        builder: CellBuilder,
        value: ProcessedUpto
    ) = builder {
        storeULong(value.lastMsgLt)
        storeBitString(value.lastMsgHash)
    }

    override fun loadTlb(
        slice: CellSlice
    ): ProcessedUpto = slice {
        val lastMsgLt = loadULong()
        val lastMsgHash = loadBitString(256)
        ProcessedUpto(lastMsgLt, lastMsgHash)
    }
}

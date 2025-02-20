package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import org.ton.tlb.providers.TlbConstructorProvider


@SerialName("msg_export_deq")
public data class MsgExportDeq(
    val outMsg: CellRef<MsgEnvelope>,
    val importBlockLt: ULong
) : OutMsg {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("msg_export_deq") {
            field("out_msg", outMsg)
            field("import_block_lt", importBlockLt)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<MsgExportDeq> by MsgExportDeqTlbConstructor
}

private object MsgExportDeqTlbConstructor : TlbConstructor<MsgExportDeq>(
    schema = "msg_export_deq\$1100 out_msg:^MsgEnvelope import_block_lt:uint63 = OutMsg;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: MsgExportDeq
    ) = builder {
        storeRef(MsgEnvelope, value.outMsg)
        storeUInt(value.importBlockLt.toLong(), 63)
    }

    override fun loadTlb(
        slice: CellSlice
    ): MsgExportDeq = slice {
        val outMsg = loadRef(MsgEnvelope)
        val importBlockLt = loadUInt(63).toLong().toULong()
        MsgExportDeq(outMsg, importBlockLt)
    }
}

package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


@SerialName("msg_export_deq_imm")
public data class MsgExportDeqImm(
    val outMsg: CellRef<MsgEnvelope>,
    val reimport: CellRef<InMsg>,
) : OutMsg {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("msg_export_deq_imm") {
            field("out_msg")
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<MsgExportDeqImm> by MsgExportDeqImmTlbConstructor
}

private object MsgExportDeqImmTlbConstructor : TlbConstructor<MsgExportDeqImm>(
    schema = "msg_export_deq_imm\$100 out_msg:^MsgEnvelope reimport:^InMsg = OutMsg;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: MsgExportDeqImm
    ) = cellBuilder {
        storeRef(MsgEnvelope, value.outMsg)
        storeRef(InMsg, value.reimport)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): MsgExportDeqImm = cellSlice {
        val outMsg = loadRef(MsgEnvelope)
        val reimport = loadRef(InMsg)
        MsgExportDeqImm(outMsg, reimport)
    }
}

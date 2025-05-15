package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


@SerialName("msg_export_tr_req")
public data class MsgExportTrReq(
    val outMsg: CellRef<MsgEnvelope>,
    val imported: CellRef<InMsg>,
) : OutMsg {
    public companion object : TlbConstructorProvider<MsgExportTrReq> by MsgExportTrReqTlbConstructor

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("msg_export_tr_req") {
            field("out_msg", outMsg)
            field("imported", imported)
        }
    }

    override fun toString(): String = print().toString()
}

private object MsgExportTrReqTlbConstructor : TlbConstructor<MsgExportTrReq>(
    schema = "msg_export_tr_req\$111 out_msg:^MsgEnvelope imported:^InMsg = OutMsg;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: MsgExportTrReq
    ) = cellBuilder {
        storeRef(MsgEnvelope, value.outMsg)
        storeRef(InMsg, value.imported)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): MsgExportTrReq = cellSlice {
        val outMsg = loadRef(MsgEnvelope)
        val imported = loadRef(InMsg)
        MsgExportTrReq(outMsg, imported)
    }
}

package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import org.ton.tlb.providers.TlbConstructorProvider

@Serializable
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

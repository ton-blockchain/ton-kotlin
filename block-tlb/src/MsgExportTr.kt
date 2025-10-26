package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider


@SerialName("msg_export_tr")
public data class MsgExportTr(
    val outMsg: CellRef<MsgEnvelope>,
    val imported: CellRef<InMsg>
) : OutMsg {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("msg_export_tr") {
            field("out_msg", outMsg)
            field("imported", imported)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<MsgExportTr> by MsgExportTrTlbConstructor
}

private object MsgExportTrTlbConstructor : TlbConstructor<MsgExportTr>(
    schema = "msg_export_tr\$011 out_msg:^MsgEnvelope imported:^InMsg = OutMsg;"
) {

    override fun storeTlb(
        builder: CellBuilder,
        value: MsgExportTr
    ) = builder {
        storeRef(MsgEnvelope, value.outMsg)
        storeRef(InMsg, value.imported)
    }

    override fun loadTlb(
        slice: CellSlice
    ): MsgExportTr = slice {
        val outMsg = loadRef(MsgEnvelope)
        val imported = loadRef(InMsg)
        MsgExportTr(outMsg, imported)
    }
}

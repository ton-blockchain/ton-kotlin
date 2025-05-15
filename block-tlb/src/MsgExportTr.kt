package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


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
        cellBuilder: CellBuilder,
        value: MsgExportTr
    ) = cellBuilder {
        storeRef(MsgEnvelope, value.outMsg)
        storeRef(InMsg, value.imported)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): MsgExportTr = cellSlice {
        val outMsg = loadRef(MsgEnvelope)
        val imported = loadRef(InMsg)
        MsgExportTr(outMsg, imported)
    }
}

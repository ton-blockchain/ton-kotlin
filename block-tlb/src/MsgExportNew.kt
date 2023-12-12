package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import org.ton.tlb.providers.TlbConstructorProvider

@Serializable
@SerialName("msg_export_new")
public data class MsgExportNew(
    val outMsg: CellRef<MsgEnvelope>,
    val transaction: CellRef<Transaction>
) : OutMsg, TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("msg_export_new") {
            field("out_msg", outMsg)
            field("transaction", transaction)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<MsgExportNew> by MsgExportNewTlbConstructor
}

private object MsgExportNewTlbConstructor : TlbConstructor<MsgExportNew>(
    schema = "msg_export_new\$001 out_msg:^MsgEnvelope transaction:^Transaction = OutMsg;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: MsgExportNew
    ) = cellBuilder {
        storeRef(MsgEnvelope, value.outMsg)
        storeRef(Transaction, value.transaction)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): MsgExportNew = cellSlice {
        val outMsg = loadRef(MsgEnvelope)
        val transaction = loadRef(Transaction)
        MsgExportNew(outMsg, transaction)
    }
}

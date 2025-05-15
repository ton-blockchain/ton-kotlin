package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.Cell
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.transaction.Transaction


@SerialName("msg_export_ext")
public data class MsgExportExt(
    val msg: CellRef<Message<Cell>>,
    val transaction: CellRef<Transaction>
) : OutMsg {
    public companion object : TlbConstructorProvider<MsgExportExt> by MsgExportExtTlbConstructor

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("msg_export_ext") {
            field("msg", msg)
            field("transaction", transaction)
        }
    }
}

private object MsgExportExtTlbConstructor : TlbConstructor<MsgExportExt>(
    schema = "msg_export_ext\$000 msg:^(Message Any) transaction:^Transaction = OutMsg;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: MsgExportExt
    ) = cellBuilder {
        storeRef(Message.Any, value.msg)
        storeRef(Transaction, value.transaction)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): MsgExportExt = cellSlice {
        val msg = loadRef(Message.Any)
        val transaction = loadRef(Transaction)
        MsgExportExt(msg, transaction)
    }
}

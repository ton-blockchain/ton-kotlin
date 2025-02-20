package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.kotlin.transaction.Transaction
import org.ton.tlb.*
import org.ton.tlb.providers.TlbConstructorProvider


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
        builder: CellBuilder,
        value: MsgExportExt
    ) = builder {
        storeRef(Message.Any, value.msg)
        storeRef(Transaction, value.transaction)
    }

    override fun loadTlb(
        slice: CellSlice
    ): MsgExportExt = slice {
        val msg = loadRef(Message.Any)
        val transaction = loadRef(Transaction)
        MsgExportExt(msg, transaction)
    }
}

@file:Suppress("NOTHING_TO_INLINE")

package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.kotlin.transaction.Transaction
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider


@SerialName("msg_import_ext")
public data class MsgImportExt(
    val msg: CellRef<Message<Cell>>,
    val transaction: CellRef<Transaction>
) : InMsg {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("msg_import_ext") {
        field("msg", msg)
        field("transaction", transaction)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<MsgImportExt> by MsgImportExtTlbConstructor
}

private object MsgImportExtTlbConstructor : TlbConstructor<MsgImportExt>(
    schema = "msg_import_ext\$000 msg:^(Message Any) transaction:^Transaction = InMsg;"
) {

    override fun storeTlb(
        builder: CellBuilder,
        value: MsgImportExt
    ) = builder {
        storeRef(Message.Any, value.msg)
        storeRef(Transaction, value.transaction)
    }

    override fun loadTlb(
        slice: CellSlice
    ): MsgImportExt = slice {
        val msg = loadRef(Message.Any)
        val transaction = loadRef(Transaction)
        MsgImportExt(msg, transaction)
    }
}

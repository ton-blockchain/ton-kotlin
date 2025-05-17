@file:Suppress("NOTHING_TO_INLINE")

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
        cellBuilder: CellBuilder,
        value: MsgImportExt
    ) = cellBuilder {
        storeRef(Message.Any, value.msg)
        storeRef(Transaction, value.transaction)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): MsgImportExt = cellSlice {
        val msg = loadRef(Message.Any)
        val transaction = loadRef(Transaction)
        MsgImportExt(msg, transaction)
    }
}

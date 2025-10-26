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


@SerialName("msg_import_ihr")
public data class MsgImportIhr(
    val msg: CellRef<Message<Cell>>,
    val transaction: CellRef<Transaction>,
    val ihrFee: Coins,
    val proofCreated: Cell
) : InMsg {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("msg_import_ihr") {
            printer.field("msg", msg)
            printer.field("transaction", transaction)
            printer.field("ihr_fee", ihrFee)
            printer.field("proof_created", proofCreated)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<MsgImportIhr> by MsgImportIhrTlbConstructor
}

private object MsgImportIhrTlbConstructor : TlbConstructor<MsgImportIhr>(
    schema = "msg_import_ihr\$010 msg:^(Message Any) transaction:^Transaction ihr_fee:Grams proof_created:^Cell = InMsg;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: MsgImportIhr
    ) = builder {
        storeRef(Message.Any, value.msg)
        storeRef(Transaction, value.transaction)
        storeTlb(Coins, value.ihrFee)
        storeRef(value.proofCreated)
    }

    override fun loadTlb(
        slice: CellSlice
    ): MsgImportIhr = slice {
        val msg = loadRef(Message.Any)
        val transaction = loadRef(Transaction)
        val ihrFee = loadTlb(Coins)
        val proofCreated = loadRef()
        MsgImportIhr(msg, transaction, ihrFee, proofCreated)
    }
}

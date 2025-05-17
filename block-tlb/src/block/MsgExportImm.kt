package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.transaction.Transaction


@SerialName("msg_export_imm")
public data class MsgExportImm(
    val outMsg: CellRef<MsgEnvelope>,
    val transaction: CellRef<Transaction>,
    val reimport: CellRef<InMsg>
) : OutMsg, TlbObject {
    public companion object : TlbConstructorProvider<MsgExportImm> by MsgExportImmTlbConstructor

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("msg_export_imm") {
            field("out_msg", outMsg)
            field("transaction", transaction)
            field("reimport", reimport)
        }
    }

    override fun toString(): String = print().toString()
}

private object MsgExportImmTlbConstructor : TlbConstructor<MsgExportImm>(
    schema = "msg_export_imm\$010 out_msg:^MsgEnvelope transaction:^Transaction reimport:^InMsg = OutMsg;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: MsgExportImm
    ) = cellBuilder {
        storeRef(MsgEnvelope, value.outMsg)
        storeRef(Transaction, value.transaction)
        storeRef(InMsg, value.reimport)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): MsgExportImm = cellSlice {
        val outMsg = loadRef(MsgEnvelope)
        val transaction = loadRef(Transaction)
        val reimport = loadRef(InMsg)
        MsgExportImm(outMsg, transaction, reimport)
    }
}

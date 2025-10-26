package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.kotlin.transaction.Transaction
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider


@SerialName("msg_import_imm")
public data class MsgImportImm(
    @SerialName("in_msg") val inMsg: CellRef<MsgEnvelope>,
    val transaction: CellRef<Transaction>,
    @SerialName("fwd_fee") val fwdFee: Coins
) : InMsg {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("msg_import_imm") {
        field("in_msg", inMsg)
        field("transaction", transaction)
        field("fwd_fee", fwdFee)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<MsgImportImm> by MsgImportImmTlbConstructor
}

private object MsgImportImmTlbConstructor : TlbConstructor<MsgImportImm>(
    schema = "msg_import_imm\$011 in_msg:^MsgEnvelope transaction:^Transaction fwd_fee:Coins = InMsg;"
) {

    override fun storeTlb(
        builder: CellBuilder,
        value: MsgImportImm
    ) = builder {
        storeRef(MsgEnvelope, value.inMsg)
        storeRef(Transaction, value.transaction)
        storeTlb(Coins, value.fwdFee)
    }

    override fun loadTlb(
        slice: CellSlice
    ): MsgImportImm = slice {
        val inMsg = loadRef(MsgEnvelope)
        val transaction = loadRef(Transaction)
        val fwdFee = loadTlb(Coins)
        MsgImportImm(inMsg, transaction, fwdFee)
    }
}

package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import org.ton.tlb.providers.TlbConstructorProvider

@Serializable
@SerialName("msg_import_fin")
public data class MsgImportFin(
    @SerialName("in_msg") val inMsg: CellRef<MsgEnvelope>,
    val transaction: CellRef<Transaction>,
    @SerialName("fwd_fee") val fwdFee: Coins
) : InMsg {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("msg_import_fin") {
            field("in_msg", inMsg)
            field("transaction", transaction)
            field("fwd_fee", fwdFee)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<MsgImportFin> by MsgImportFinTlbConstructor
}

private object MsgImportFinTlbConstructor : TlbConstructor<MsgImportFin>(
    schema = "msg_import_fin\$100 in_msg:^MsgEnvelope transaction:^Transaction fwd_fee:Coins = InMsg;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: MsgImportFin
    ) = cellBuilder {
        storeRef(MsgEnvelope, value.inMsg)
        storeRef(Transaction, value.transaction)
        storeTlb(Coins, value.fwdFee)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): MsgImportFin = cellSlice {
        val inMsg = loadRef(MsgEnvelope)
        val transaction = loadRef(Transaction)
        val fwdFee = loadTlb(Coins)
        MsgImportFin(inMsg, transaction, fwdFee)
    }
}

package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


@SerialName("msg_import_tr")
public data class MsgImportTr(
    @SerialName("in_msg") val inMsg: CellRef<MsgEnvelope>,
    @SerialName("out_msg") val outMsg: CellRef<MsgEnvelope>,
    @SerialName("transit_fee") val transitFee: Coins
) : InMsg {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("msg_import_tr") {
            field("in_msg", inMsg)
            field("out_msg", outMsg)
            field("transit_fee", transitFee)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<MsgImportTr> by MsgImportTrTlbConstructor
}

private object MsgImportTrTlbConstructor : TlbConstructor<MsgImportTr>(
    schema = "msg_import_tr\$101  in_msg:^MsgEnvelope out_msg:^MsgEnvelope transit_fee:Coins = InMsg;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: MsgImportTr
    ) = cellBuilder {
        storeRef(MsgEnvelope, value.inMsg)
        storeRef(MsgEnvelope, value.outMsg)
        storeTlb(Coins, value.transitFee)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): MsgImportTr = cellSlice {
        val inMsg = loadRef(MsgEnvelope)
        val outMsg = loadRef(MsgEnvelope)
        val transitFee = loadTlb(Coins)
        MsgImportTr(inMsg, outMsg, transitFee)
    }
}

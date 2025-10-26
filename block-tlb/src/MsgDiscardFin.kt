package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider


@SerialName("msg_discard_fin")
public data class MsgDiscardFin(
    val inMsg: CellRef<MsgEnvelope>,
    val transactionId: ULong,
    val fwdFee: Coins
) : InMsg {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("msg_discard_fin") {
            field("in_msg", inMsg)
            field("transaction_id", transactionId)
            field("fwd_fee", fwdFee)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<MsgDiscardFin> by MsgDiscardFinTlbConstructor
}

private object MsgDiscardFinTlbConstructor : TlbConstructor<MsgDiscardFin>(
    schema = "msg_discard_fin\$110 in_msg:^MsgEnvelope transaction_id:uint64 fwd_fee:Coins = InMsg;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: MsgDiscardFin
    ) = builder {
        storeRef(MsgEnvelope, value.inMsg)
        storeULong(value.transactionId)
        storeTlb(Coins, value.fwdFee)
    }

    override fun loadTlb(
        slice: CellSlice
    ): MsgDiscardFin = slice {
        val inMsg = loadRef(MsgEnvelope)
        val transactionId = loadULong()
        val fwdFee = loadTlb(Coins)
        MsgDiscardFin(inMsg, transactionId, fwdFee)
    }
}

package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


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
        cellBuilder: CellBuilder,
        value: MsgDiscardFin
    ) = cellBuilder {
        storeRef(MsgEnvelope, value.inMsg)
        storeUInt64(value.transactionId)
        storeTlb(Coins, value.fwdFee)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): MsgDiscardFin = cellSlice {
        val inMsg = loadRef(MsgEnvelope)
        val transactionId = loadUInt64()
        val fwdFee = loadTlb(Coins)
        MsgDiscardFin(inMsg, transactionId, fwdFee)
    }
}

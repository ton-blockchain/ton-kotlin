package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbCombinatorProvider


@SerialName("msg_envelope")
public data class MsgEnvelope(
    @SerialName("cur_addr") val curAddr: IntermediateAddress,
    @SerialName("next_addr") val nextAddr: IntermediateAddress,
    @SerialName("fwd_fee_remaining") val fwdFeeRemaining: Coins,
    val msg: CellRef<Message<Cell>>
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("msg_envelope") {
            field("cur_addr", curAddr)
            field("next_addr", nextAddr)
            field("fwd_fee_remaining", fwdFeeRemaining)
            field("msg", msg)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbCombinatorProvider<MsgEnvelope> by MsgEnvelopeTlbConstructor.asTlbCombinator()
}

private object MsgEnvelopeTlbConstructor : TlbConstructor<MsgEnvelope>(
    schema = "msg_envelope#4 cur_addr:IntermediateAddress " +
            "next_addr:IntermediateAddress fwd_fee_remaining:Coins " +
            "msg:^(Message Any) = MsgEnvelope;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: MsgEnvelope
    ) = builder {
        storeTlb(IntermediateAddress, value.curAddr)
        storeTlb(IntermediateAddress, value.nextAddr)
        storeTlb(Coins, value.fwdFeeRemaining)
        storeRef(Message.Any, value.msg)
    }

    override fun loadTlb(
        slice: CellSlice
    ): MsgEnvelope = slice {
        val curAddr = loadTlb(IntermediateAddress)
        val nextAddr = loadTlb(IntermediateAddress)
        val fwdFeeRemaining = loadTlb(Coins)
        val msg = loadRef(Message.Any)
        MsgEnvelope(curAddr, nextAddr, fwdFeeRemaining, msg)
    }
}

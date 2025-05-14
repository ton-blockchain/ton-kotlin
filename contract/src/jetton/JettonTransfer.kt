package org.ton.contract.jetton

import org.ton.bigint.toBigInt
import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbConstructor

public const val OP_JETTON_TRANSFER: Int = 0xf8a7ea5

public data class JettonTransfer(
    val queryId: ULong,
    val amount: Coins,
    val toAddress: MsgAddressInt,
    val responseAddress: MsgAddressInt,
    val forwardAmount: Coins,
    val forwardPayload: Cell?,
    val customPayload: Cell?
) {
    public companion object : TlbConstructor<JettonTransfer>(
        "jetton_transfer query_id:uint64 amount:coins to_address:MsgAddress response_address:MsgAddress custom_payload:Maybe ^Cell forward_amount:coins forward_payload:Maybe ^Cell = JettonTransfer"
    ) {
        override fun loadTlb(cellSlice: CellSlice): JettonTransfer {
            val opCode = cellSlice.loadUInt(32)
            require(opCode == OP_JETTON_TRANSFER.toBigInt()) { "Invalid op code" }
            
            val queryId = cellSlice.loadULong()
            val amount = Coins.loadTlb(cellSlice)
            val toAddress = MsgAddressInt.loadTlb(cellSlice)
            val responseAddress = MsgAddressInt.loadTlb(cellSlice)
            val customPayload = cellSlice.loadNullableRef()
            val forwardAmount = Coins.loadTlb(cellSlice)
            val forwardPayload = cellSlice.loadNullableRef()

            return JettonTransfer(
                queryId = queryId,
                amount = amount,
                toAddress = toAddress,
                responseAddress = responseAddress,
                forwardAmount = forwardAmount,
                forwardPayload = forwardPayload,
                customPayload = customPayload
            )
        }

        override fun storeTlb(cellBuilder: CellBuilder, value: JettonTransfer) {
            cellBuilder.storeUInt(OP_JETTON_TRANSFER, 32)
            cellBuilder.storeULong(value.queryId)
            Coins.storeTlb(cellBuilder, value.amount)
            MsgAddressInt.storeTlb(cellBuilder, value.toAddress)
            MsgAddressInt.storeTlb(cellBuilder, value.responseAddress)
            cellBuilder.storeNullableRef(value.customPayload)
            Coins.storeTlb(cellBuilder, value.forwardAmount)
            cellBuilder.storeNullableRef(value.forwardPayload)
        }
    }
}

package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbPrettyPrinter
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb

@SerialName("int_msg_info")

public data class IntMsgInfo(
    @SerialName("ihr_disabled") val ihrDisabled: Boolean,
    val bounce: Boolean,
    val bounced: Boolean,
    val src: MsgAddressInt,
    val dest: MsgAddressInt,
    val value: CurrencyCollection,
    @SerialName("ihr_fee") val ihr_fee: Coins,
    @SerialName("fwd_fee") val fwd_fee: Coins,
    @SerialName("created_lt") val created_lt: Long,
    @SerialName("created_at") val created_at: Int
) : CommonMsgInfo {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("int_msg_info") {
            field("ihr_disabled", ihrDisabled)
            field("bounce", bounce)
            field("bounced", bounced)
            field("src", src)
            field("dest", dest)
            field("value", value)
            field("ihr_fee", ihr_fee)
            field("fwd_fee", fwd_fee)
            field("created_lt", created_lt)
            field("created_at", created_at)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<IntMsgInfo> by IntMsgInfoTlbConstructor
}

private object IntMsgInfoTlbConstructor : TlbConstructor<IntMsgInfo>(
    schema = "int_msg_info\$0 ihr_disabled:Bool bounce:Bool bounced:Bool src:MsgAddressInt dest:MsgAddressInt value:CurrencyCollection ihr_fee:Coins fwd_fee:Coins created_lt:uint64 created_at:uint32 = CommonMsgInfo;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder, value: IntMsgInfo
    ) = cellBuilder {
        storeBit(value.ihrDisabled)
        storeBit(value.bounce)
        storeBit(value.bounced)
        storeTlb(MsgAddressInt, value.src)
        storeTlb(MsgAddressInt, value.dest)
        storeTlb(CurrencyCollection, value.value)
        storeTlb(Coins, value.ihr_fee)
        storeTlb(Coins, value.fwd_fee)
        storeUInt(value.created_lt, 64)
        storeUInt(value.created_at, 32)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): IntMsgInfo = cellSlice {
        val ihrDisabled = loadBit()
        val bounce = loadBit()
        val bounced = loadBit()
        val src = loadTlb(MsgAddressInt)
        val dest = loadTlb(MsgAddressInt)
        val value = loadTlb(CurrencyCollection)
        val ihrFee = loadTlb(Coins)
        val fwdFee = loadTlb(Coins)
        val createdLt = loadUInt(64).toLong()
        val createdAt = loadUInt(32).toInt()
        IntMsgInfo(
            ihrDisabled, bounce, bounced, src, dest, value, ihrFee, fwdFee, createdLt, createdAt
        )
    }
}

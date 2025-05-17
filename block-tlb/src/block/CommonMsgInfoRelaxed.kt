@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider

@JsonClassDiscriminator("@type")

public sealed interface CommonMsgInfoRelaxed : TlbObject {
    @SerialName("int_msg_info")
    public data class IntMsgInfoRelaxed(
        val ihrDisabled: Boolean = true,
        val bounce: Boolean = true,
        val bounced: Boolean = false,
        val src: MsgAddress = MsgAddressExt(),
        val dest: MsgAddressInt = AddrStd(),
        val value: CurrencyCollection = CurrencyCollection.ZERO,
        val ihrFee: Coins = Coins(),
        val fwdFee: Coins = Coins(),
        val createdLt: ULong = 0u,
        val createdAt: UInt = 0u
    ) : CommonMsgInfoRelaxed {
        public constructor(dest: MsgAddressInt, bounce: Boolean, value: Coins) : this(
            dest = dest,
            bounce = bounce,
            value = CurrencyCollection(value, ExtraCurrencyCollection.EMPTY)
        )

        public constructor(dest: MsgAddressInt, bounce: Boolean, value: CurrencyCollection) : this(
            ihrDisabled = true,
            bounce = bounce,
            bounced = false,
            src = MsgAddressExt(),
            dest = dest,
            value = value
        )

        override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
            type("int_msg_info") {
                field("ihr_disabled", ihrDisabled)
                field("bounce", bounce)
                field("bounced", bounced)
                field("src", src)
                field("dest", dest)
                field("value", value)
                field("ihr_fee", ihrFee)
                field("fwd_fee", fwdFee)
                field("created_lt", createdLt)
                field("created_at", createdAt)
            }
        }

        override fun toString(): String = print().toString()
    }

    @SerialName("ext_out_msg_info")
    public data class ExtOutMsgInfoRelaxed(
        val src: MsgAddress,
        val dest: MsgAddressExt,
        val createdLt: ULong,
        val createdAt: UInt
    ) : CommonMsgInfoRelaxed {
        override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
            type("ext_out_msg_info") {
                field("src", src)
                field("dest", dest)
                field("created_lt", createdLt)
                field("created_at", createdAt)
            }
        }

        override fun toString(): String = print().toString()
    }

    public companion object : TlbCombinatorProvider<CommonMsgInfoRelaxed> by CommonMsgInfoRelaxedTlbCombinator
}

private object CommonMsgInfoRelaxedTlbCombinator : TlbCombinator<CommonMsgInfoRelaxed>(
    CommonMsgInfoRelaxed::class,
    CommonMsgInfoRelaxed.IntMsgInfoRelaxed::class to IntMsgInfoTlbConstructor,
    CommonMsgInfoRelaxed.ExtOutMsgInfoRelaxed::class to ExtOutMsgInfoTlbConstructor
) {
    private object IntMsgInfoTlbConstructor : TlbConstructor<CommonMsgInfoRelaxed.IntMsgInfoRelaxed>(
        schema = "int_msg_info\$0 ihr_disabled:Bool bounce:Bool bounced:Bool" +
                " src:MsgAddress dest:MsgAddressInt" +
                " value:CurrencyCollection ihr_fee:Coins fwd_fee:Coins" +
                " created_lt:uint64 created_at:uint32 = CommonMsgInfoRelaxed;"
    ) {
        override fun storeTlb(
            cellBuilder: CellBuilder, value: CommonMsgInfoRelaxed.IntMsgInfoRelaxed
        ) = cellBuilder {
            storeBoolean(value.ihrDisabled)
            storeBoolean(value.bounce)
            storeBoolean(value.bounced)
            storeTlb(MsgAddress, value.src)
            storeTlb(MsgAddressInt, value.dest)
            storeTlb(CurrencyCollection, value.value)
            storeTlb(Coins, value.ihrFee)
            storeTlb(Coins, value.fwdFee)
            storeUInt64(value.createdLt)
            storeUInt32(value.createdAt)
        }

        override fun loadTlb(
            cellSlice: CellSlice
        ): CommonMsgInfoRelaxed.IntMsgInfoRelaxed = cellSlice {
            val ihrDisabled = loadBit()
            val bounce = loadBit()
            val bounced = loadBit()
            val src = loadTlb(MsgAddress)
            val dest = loadTlb(MsgAddressInt)
            val value = loadTlb(CurrencyCollection)
            val ihrFee = loadTlb(Coins)
            val fwdFee = loadTlb(Coins)
            val createdLt = loadUInt64()
            val createdAt = loadUInt32()
            CommonMsgInfoRelaxed.IntMsgInfoRelaxed(
                ihrDisabled, bounce, bounced, src, dest, value, ihrFee, fwdFee, createdLt, createdAt
            )
        }
    }

    private object ExtOutMsgInfoTlbConstructor : TlbConstructor<CommonMsgInfoRelaxed.ExtOutMsgInfoRelaxed>(
        schema = "ext_out_msg_info\$11 src:MsgAddress dest:MsgAddressExt" +
                " created_lt:uint64 created_at:uint32 = CommonMsgInfoRelaxed;"
    ) {

        override fun storeTlb(
            cellBuilder: CellBuilder, value: CommonMsgInfoRelaxed.ExtOutMsgInfoRelaxed
        ) = cellBuilder {
            storeTlb(MsgAddress, value.src)
            storeTlb(MsgAddressExt, value.dest)
            storeUInt64(value.createdLt)
            storeUInt32(value.createdAt)
        }

        override fun loadTlb(
            cellSlice: CellSlice
        ): CommonMsgInfoRelaxed.ExtOutMsgInfoRelaxed = cellSlice {
            val src = loadTlb(MsgAddress)
            val dest = loadTlb(MsgAddressExt)
            val createdLt = loadUInt64()
            val createdAt = loadUInt32()
            CommonMsgInfoRelaxed.ExtOutMsgInfoRelaxed(src, dest, createdLt, createdAt)
        }
    }
}

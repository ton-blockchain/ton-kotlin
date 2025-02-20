package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.kotlin.message.address.AddrExtern
import org.ton.kotlin.message.address.MsgAddressInt
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbPrettyPrinter
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb

@SerialName("ext_out_msg_info")

public data class ExtOutMsgInfo(
    val src: MsgAddressInt,
    val dest: AddrExtern?,
    @SerialName("created_lt") val createdLt: ULong,
    @SerialName("created_at") val createdAt: UInt
) : CommonMsgInfo {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("ext_out_msg_info") {
            field("src", src)
            field("dest", dest)
            field("created_lt", createdLt)
            field("created_at", createdAt)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<ExtOutMsgInfo> by ExtOutMsgInfoTlbConstructor
}

private object ExtOutMsgInfoTlbConstructor : TlbConstructor<ExtOutMsgInfo>(
    schema = "ext_out_msg_info\$11 src:MsgAddressInt dest:MsgAddressExt created_lt:uint64 created_at:uint32 = CommonMsgInfo;"
) {
    override fun storeTlb(
        builder: CellBuilder, value: ExtOutMsgInfo
    ) = builder {
        storeTlb(MsgAddressInt, value.src)
        storeTlb(MsgAddressExt, value.dest)
        storeUInt(value.createdLt.toLong(), 64)
        storeUInt(value.createdAt.toInt(), 32)
    }

    override fun loadTlb(
        slice: CellSlice
    ): ExtOutMsgInfo = slice {
        val src = loadTlb(MsgAddressInt)
        val dest = loadTlb(MsgAddressExt)
        val createdLt = loadULong()
        val createdAt = loadUInt32()
        ExtOutMsgInfo(src, dest, createdLt, createdAt)
    }
}

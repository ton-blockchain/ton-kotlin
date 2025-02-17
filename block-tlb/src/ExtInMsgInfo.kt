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

@SerialName("ext_in_msg_info")
public data class ExtInMsgInfo(
    val src: AddrExtern?,
    val dest: MsgAddressInt,
    @SerialName("import_fee") val importFee: Coins
) : CommonMsgInfo {
    public constructor(dest: MsgAddressInt) : this(null, dest)

    public constructor(
        src: AddrExtern?,
        dest: MsgAddressInt,
    ) : this(src, dest, Coins.ZERO)

    public constructor(
        dest: MsgAddressInt,
        importFee: Coins = Coins.ZERO
    ) : this(null, dest, importFee)

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("ext_in_msg_info") {
            field("src", src)
            field("dest", dest)
            field("import_fee", importFee)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<ExtInMsgInfo> by ExtInMsgInfoTlbConstructor
}

private object ExtInMsgInfoTlbConstructor : TlbConstructor<ExtInMsgInfo>(
    schema = "ext_in_msg_info\$10 src:MsgAddressExt dest:MsgAddressInt import_fee:Coins = CommonMsgInfo;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder, value: ExtInMsgInfo
    ) = cellBuilder {
        storeTlb(AddrExtern, value.src)
        storeTlb(MsgAddressInt, value.dest)
        storeTlb(Coins, value.importFee)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): ExtInMsgInfo = cellSlice {
        val src = loadTlb(AddrExtern)
        val dest = loadTlb(MsgAddressInt)
        val importFee = loadTlb(Coins)
        ExtInMsgInfo(src, dest, importFee)
    }
}

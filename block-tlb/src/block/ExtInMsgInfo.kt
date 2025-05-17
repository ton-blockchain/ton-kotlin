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


@SerialName("ext_in_msg_info")
public data class ExtInMsgInfo(
    val src: MsgAddressExt,
    val dest: MsgAddressInt,
    @SerialName("import_fee") val importFee: Coins
) : CommonMsgInfo {
    public constructor(dest: MsgAddressInt) : this(null, dest)

    public constructor(
        src: AddrExtern?,
        dest: MsgAddressInt,
    ) : this(src ?: AddrNone, dest, Coins.ZERO)

    public constructor(
        dest: MsgAddressInt,
        importFee: Coins = Coins.ZERO
    ) : this(AddrNone, dest, importFee)

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
        storeTlb(MsgAddressExt, value.src)
        storeTlb(MsgAddressInt, value.dest)
        storeTlb(Coins, value.importFee)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): ExtInMsgInfo = cellSlice {
        val src = loadTlb(MsgAddressExt)
        val dest = loadTlb(MsgAddressInt)
        val importFee = loadTlb(Coins)
        ExtInMsgInfo(src, dest, importFee)
    }
}

//1000100000000000101100100010011101000110010110110101100001101000110011000110001100000001100110100101000100111000001000000001100001101000101110011010101010010001010001001101000110001000100011111110101011100010101111110101101101101010101000011100111011111001110010011001111
//1000100000000001011010100101010000101101011101000110111001101010101000000011101111100011001100110010111010101010000111001111110110101001100010000010111010101001110111001010000000111011110110110001010100001000001000010001000110011100100001001111000101101100110110111100100000000111011100001111111111010011011110101011010110100100011010111101110000101001100110100011111001001110000010000110010111100100010001111001111010001101110101010011111000010101100110110101011111111011010010100011111011010100001000001101110000011101110110011100001010001111110111010101010010000110101110011100001100100110001100001100111000111110101101001001001000010000111100000101111001111001100000100100011010111100110011101000101011110101000110001100101010100110000110111001001010001000111010110100000010010000001010000000000000000000000000000000000000011

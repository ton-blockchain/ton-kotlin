package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.bitstring.toBitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSize
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbPrettyPrinter
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import kotlin.jvm.JvmName


@SerialName("addr_extern")
public data class AddrExtern(
    @SerialName("len")
    val len: Int, // len : ## 9

    @SerialName("external_address")
    @get:JvmName("externalAddress")
    val externalAddress: BitString // external_address : bits len
) : MsgAddressExt {
    init {
        require(externalAddress.size == len) { "required: external_address.size == len, actual: ${externalAddress.size}" }
    }

    public constructor(externalAddress: ByteArray) : this(externalAddress.toBitString())
    public constructor(externalAddress: BitString) : this(externalAddress.size, externalAddress)

    override val cellSize: CellSize get() = CellSize(9 + len, 0)

    override fun toString(): String = print().toString()

    public override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("addr_extern") {
            field("len", len)
            field("external_address", externalAddress)
        }
    }

    public companion object : TlbConstructorProvider<AddrExtern> by AddrExternTlbConstructor
}

private object AddrExternTlbConstructor : TlbConstructor<AddrExtern>(
    schema = "addr_extern$01 len:(## 9) external_address:(bits len) = MsgAddressExt;"
) {
    override fun storeTlb(
        builder: CellBuilder, value: AddrExtern
    ) = builder {
        storeUInt(value.len, 9)
        storeBitString(value.externalAddress)
    }

    override fun loadTlb(
        slice: CellSlice
    ): AddrExtern = slice {
        val len = loadUInt(9).toInt()
        val externalAddress = loadBitString(len)
        AddrExtern(len, externalAddress)
    }
}

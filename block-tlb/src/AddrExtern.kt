package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.bitstring.BitString
import org.ton.bitstring.toBitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.kotlin.cell.CellSize
import org.ton.sdk.bigint.toInt
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbPrettyPrinter
import org.ton.tlb.providers.TlbConstructorProvider
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

    public override fun print(tlbPrettyPrinter: TlbPrettyPrinter): TlbPrettyPrinter = tlbPrettyPrinter {
        type("addr_extern") {
            field("len", len)
            field("external_address", externalAddress)
        }
    }

    public companion object : TlbConstructorProvider<AddrExtern> by AddrExternTlbConstructor
}

private object AddrExternTlbConstructor : TlbConstructor<AddrExtern>(
    schema = "addr_extern\$01 len:(## 9) external_address:(bits len) = MsgAddressExt;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder, value: AddrExtern
    ) = cellBuilder {
        storeUInt(value.len, 9)
        storeBits(value.externalAddress)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): AddrExtern = cellSlice {
        val len = loadUInt(9).toInt()
        val externalAddress = loadBits(len)
        AddrExtern(len, externalAddress)
    }
}

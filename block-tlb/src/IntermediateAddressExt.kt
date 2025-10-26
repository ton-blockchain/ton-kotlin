package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.bigint.toInt
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbPrettyPrinter
import org.ton.tlb.providers.TlbConstructorProvider


@SerialName("interm_addr_ext")
public data class IntermediateAddressExt(
    @SerialName("workchain_id") val workchainId: Int,
    @SerialName("addr_pfx") val addrPfx: ULong
) : IntermediateAddress {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("interm_addr_ext") {
        field("workchain_id", workchainId)
        field("addr_pfx", addrPfx)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<IntermediateAddressExt> by IntermediateAddressExtTlbConstructor
}

private object IntermediateAddressExtTlbConstructor : TlbConstructor<IntermediateAddressExt>(
    schema = "interm_addr_ext\$11 workchain_id:int32 addr_pfx:uint64 = IntermediateAddress;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: IntermediateAddressExt
    ) = cellBuilder {
        storeInt(value.workchainId, 32)
        storeUInt64(value.addrPfx)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): IntermediateAddressExt = cellSlice {
        val workchainId = loadInt(32).toInt()
        val addrPfx = loadUInt64()
        IntermediateAddressExt(workchainId, addrPfx)
    }
}

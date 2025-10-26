package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.sdk.bigint.toInt
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbPrettyPrinter
import org.ton.tlb.providers.TlbConstructorProvider


@SerialName("interm_addr_simple")
public data class IntermediateAddressSimple(
    @SerialName("workchain_id") val workchainId: Int,
    @SerialName("addr_pfx") val addrPfx: ULong
) : IntermediateAddress {
    public companion object :
        TlbConstructorProvider<IntermediateAddressSimple> by IntermediateAddressSimpleTlbConstructor

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("interm_addr_simple") {
        field("workchain_id", workchainId)
        field("addr_pfx", addrPfx)
    }
}

private object IntermediateAddressSimpleTlbConstructor : TlbConstructor<IntermediateAddressSimple>(
    schema = "interm_addr_simple\$10 workchain_id:int8 addr_pfx:uint64 = IntermediateAddress;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder, value: IntermediateAddressSimple
    ) = cellBuilder {
        storeInt(value.workchainId, 8)
        storeUInt64(value.addrPfx)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): IntermediateAddressSimple = cellSlice {
        val workchainId = loadInt(8).toInt()
        val addrPfx = loadUInt64()
        IntermediateAddressSimple(workchainId, addrPfx)
    }
}

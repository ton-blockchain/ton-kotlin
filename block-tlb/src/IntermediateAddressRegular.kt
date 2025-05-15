package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbPrettyPrinter
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


@SerialName("interm_addr_regular")
public data class IntermediateAddressRegular(
    @SerialName("use_dest_bits") val useDestBits: Int
) : IntermediateAddress {
    init {
        require(useDestBits <= 96) { "expected: use_dest_bits <= 96, actual: $useDestBits" }
    }

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("interm_addr_regular") {
        field("use_dest_bits", useDestBits)
    }

    override fun toString(): String = print().toString()

    public companion object :
        TlbConstructorProvider<IntermediateAddressRegular> by IntermediateAddressRegularTlbConstructor
}

private object IntermediateAddressRegularTlbConstructor : TlbConstructor<IntermediateAddressRegular>(
    schema = "interm_addr_regular\$0 use_dest_bits:(#<= 96) = IntermediateAddress;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: IntermediateAddressRegular
    ) = cellBuilder {
        storeUIntLeq(value.useDestBits, 96)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): IntermediateAddressRegular = cellSlice {
        val useDestBits = loadUIntLeq(96).toInt()
        IntermediateAddressRegular(useDestBits)
    }
}

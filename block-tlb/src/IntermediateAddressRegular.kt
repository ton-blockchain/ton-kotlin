package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.sdk.bigint.toInt
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbPrettyPrinter
import org.ton.tlb.providers.TlbConstructorProvider


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
        builder: CellBuilder,
        value: IntermediateAddressRegular
    ) = builder {
        storeUIntLeq(value.useDestBits, 96)
    }

    override fun loadTlb(
        slice: CellSlice
    ): IntermediateAddressRegular = slice {
        val useDestBits = loadUIntLeq(96).toInt()
        IntermediateAddressRegular(useDestBits)
    }
}

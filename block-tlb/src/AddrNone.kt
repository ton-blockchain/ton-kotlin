package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.cell.CellSize
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbPrettyPrinter
import org.ton.tlb.providers.TlbConstructorProvider

@SerialName("addr_none")

public object AddrNone : MsgAddressExt, TlbConstructorProvider<AddrNone> by AddrNoneTlbConstructor {
    override val cellSize: CellSize = CellSize(2, 0)

    override fun toString(): String = print().toString()

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("addr_none")
    }
}

private object AddrNoneTlbConstructor : TlbConstructor<AddrNone>(
    schema = "addr_none\$00 = MsgAddressExt;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: AddrNone
    ) {
    }

    override fun loadTlb(slice: CellSlice): AddrNone {
        return AddrNone
    }
}

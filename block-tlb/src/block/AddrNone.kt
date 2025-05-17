package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSize
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbPrettyPrinter
import org.ton.kotlin.tlb.providers.TlbConstructorProvider

@SerialName("addr_none")

public object AddrNone : MsgAddressExt, TlbConstructorProvider<AddrNone> by AddrNoneTlbConstructor {
    override val cellSize: CellSize = CellSize(2, 0)

    override fun toString(): String = print().toString()

    override fun print(tlbPrettyPrinter: TlbPrettyPrinter): TlbPrettyPrinter = tlbPrettyPrinter {
        type("addr_none")
    }
}

private object AddrNoneTlbConstructor : TlbConstructor<AddrNone>(
    schema = "addr_none\$00 = MsgAddressExt;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: AddrNone
    ) {
    }

    override fun loadTlb(cellSlice: CellSlice): AddrNone {
        return AddrNone
    }
}

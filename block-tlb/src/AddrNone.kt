package org.ton.block

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider

@Deprecated("Use ExtAddr? instead", ReplaceWith("AddrInternal?"))
public object AddrNone : TlbConstructorProvider<AddrNone> by AddrNoneTlbConstructor

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

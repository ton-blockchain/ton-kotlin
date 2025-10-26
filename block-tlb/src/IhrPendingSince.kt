package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider


@SerialName("ihr_pending")
public data class IhrPendingSince(
    val import_lt: ULong
) {
    public companion object : TlbConstructorProvider<IhrPendingSince> by IhrPendingSinceTlbConstructor
}

private object IhrPendingSinceTlbConstructor : TlbConstructor<IhrPendingSince>(
    schema = "ihr_pending\$_ import_lt:uint64 = IhrPendingSince;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: IhrPendingSince
    ) = builder {
        storeULong(value.import_lt)
    }

    override fun loadTlb(
        slice: CellSlice
    ): IhrPendingSince = slice {
        val importLt = loadULong()
        IhrPendingSince(importLt)
    }
}

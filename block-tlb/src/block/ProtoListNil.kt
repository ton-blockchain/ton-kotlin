package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.providers.TlbConstructorProvider

@SerialName("proto_list_nil")

public object ProtoListNil : ProtoList, TlbConstructorProvider<ProtoListNil> by ProtoListNilTlbConstructor {
    override fun iterator(): Iterator<Protocol> = iterator {}
}

private object ProtoListNilTlbConstructor : org.ton.kotlin.tlb.TlbConstructor<ProtoListNil>(
    schema = "proto_list_nil#0 = ProtoList;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: ProtoListNil
    ) {
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): ProtoListNil = ProtoListNil
}

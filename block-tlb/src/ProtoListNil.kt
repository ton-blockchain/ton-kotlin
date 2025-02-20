package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.providers.TlbConstructorProvider

@SerialName("proto_list_nil")

public object ProtoListNil : ProtoList, TlbConstructorProvider<ProtoListNil> by ProtoListNilTlbConstructor {
    override fun iterator(): Iterator<Protocol> = iterator {}
}

private object ProtoListNilTlbConstructor : org.ton.tlb.TlbConstructor<ProtoListNil>(
    schema = "proto_list_nil#0 = ProtoList;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: ProtoListNil
    ) {
    }

    override fun loadTlb(
        slice: CellSlice
    ): ProtoListNil = ProtoListNil
}

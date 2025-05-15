package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider

@SerialName("proto_http")

public object ProtoHttp : Protocol, TlbConstructorProvider<ProtoHttp> by ProtoHttpTlbConstructor

private object ProtoHttpTlbConstructor : TlbConstructor<ProtoHttp>(
    schema = "proto_http#4854 = Protocol;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: ProtoHttp
    ) {
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): ProtoHttp = ProtoHttp
}

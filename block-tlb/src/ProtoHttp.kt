package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider

@SerialName("proto_http")
@Serializable
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

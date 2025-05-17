package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb

@SerialName("proto_list_next")

public data class ProtoListNext(
    val head: Protocol,
    val tail: ProtoList
) : ProtoList {
    override fun iterator(): Iterator<Protocol> = iterator {
        yield(head)
        yieldAll(tail)
    }

    public companion object : TlbConstructorProvider<ProtoListNext> by ProtoListNextTlbConstructor
}

private object ProtoListNextTlbConstructor : org.ton.kotlin.tlb.TlbConstructor<ProtoListNext>(
    schema = "proto_list_next#1 head:Protocol tail:ProtoList = ProtoList;"
) {
    override fun storeTlb(
        cellBuilder: org.ton.kotlin.cell.CellBuilder,
        value: ProtoListNext
    ) {
        cellBuilder.storeTlb(Protocol, value.head)
        cellBuilder.storeTlb(ProtoList, value.tail)
    }

    override fun loadTlb(
        cellSlice: org.ton.kotlin.cell.CellSlice
    ): ProtoListNext {
        val head = cellSlice.loadTlb(Protocol)
        val tail = cellSlice.loadTlb(ProtoList)
        return ProtoListNext(head, tail)
    }
}

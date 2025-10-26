package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb

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

private object ProtoListNextTlbConstructor : org.ton.tlb.TlbConstructor<ProtoListNext>(
    schema = "proto_list_next#1 head:Protocol tail:ProtoList = ProtoList;"
) {
    override fun storeTlb(
        builder: org.ton.cell.CellBuilder,
        value: ProtoListNext
    ) {
        builder.storeTlb(Protocol, value.head)
        builder.storeTlb(ProtoList, value.tail)
    }

    override fun loadTlb(
        slice: org.ton.cell.CellSlice
    ): ProtoListNext {
        val head = slice.loadTlb(Protocol)
        val tail = slice.loadTlb(ProtoList)
        return ProtoListNext(head, tail)
    }
}

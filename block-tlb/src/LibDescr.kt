package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.hashmap.HmEdge
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb


@SerialName("shared_lib_descr")
public data class LibDescr(
    val lib: Cell,
    val publishers: HmEdge<Unit>
) {
    public companion object : TlbConstructorProvider<LibDescr> by LibDescrTlbConstructor
}

private object LibDescrTlbConstructor : TlbConstructor<LibDescr>(
    schema = "shared_lib_descr\$00 lib:^Cell publishers:(Hashmap 256 True) = LibDescr;"
) {
    val publishers by lazy {
        HmEdge.tlbCodec(256, object : TlbCodec<Unit> {
            override fun storeTlb(builder: CellBuilder, value: Unit) {
            }

            override fun loadTlb(slice: CellSlice) {
            }
        })
    }

    override fun storeTlb(
        builder: CellBuilder,
        value: LibDescr
    ) = builder {
        storeRef(value.lib)
        storeTlb(publishers, value.publishers)
    }

    override fun loadTlb(
        slice: CellSlice
    ): LibDescr = slice {
        val lib = loadRef()
        val publishers = loadTlb(publishers)
        LibDescr(lib, publishers)
    }
}

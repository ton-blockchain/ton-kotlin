package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.Cell
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.hashmap.HmEdge
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


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
            override fun storeTlb(cellBuilder: CellBuilder, value: Unit) {
            }

            override fun loadTlb(cellSlice: CellSlice) {
            }
        })
    }

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: LibDescr
    ) = cellBuilder {
        storeRef(value.lib)
        publishers.storeTlb(cellBuilder, value.publishers)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): LibDescr = cellSlice {
        val lib = loadRef()
        val publishers = loadTlb(publishers)
        LibDescr(lib, publishers)
    }
}

package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


@SerialName("prev_blks_info")
public data class PrevBlksInfo(
    val prev1: CellRef<ExtBlkRef>, // prev1 : ^ExtBlkRef
    val prev2: CellRef<ExtBlkRef> // prev2 : ^ExtBlkRef
) : BlkPrevInfo {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("prev_blks_info") {
        field("prev1", prev1)
        field("prev2", prev2)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<PrevBlksInfo> by PrevBlksInfoTlbConstructor
}

private object PrevBlksInfoTlbConstructor : TlbConstructor<PrevBlksInfo>(
    schema = "prev_blks_info\$_ prev1:^ExtBlkRef prev2:^ExtBlkRef = BlkPrevInfo 1;"
) {
    private val cellRef = CellRef.tlbCodec(ExtBlkRef)

    override fun storeTlb(
        builder: CellBuilder,
        value: PrevBlksInfo
    ) = builder {
        storeTlb(cellRef, value.prev1)
        storeTlb(cellRef, value.prev2)
    }

    override fun loadTlb(
        slice: CellSlice
    ): PrevBlksInfo = slice {
        val prev1 = loadTlb(cellRef)
        val prev2 = loadTlb(cellRef)
        PrevBlksInfo(prev1, prev2)
    }
}

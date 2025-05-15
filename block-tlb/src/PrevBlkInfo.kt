package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbPrettyPrinter
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb


@SerialName("prev_blk_info")
public data class PrevBlkInfo(
    val prev: ExtBlkRef // prev : ExtBlkRef
) : BlkPrevInfo {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("prev_blk_info") {
        field("prev", prev)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<PrevBlkInfo> by PrevBlkInfoTlbConstructor
}

private object PrevBlkInfoTlbConstructor : TlbConstructor<PrevBlkInfo>(
    schema = "prev_blk_info\$_ prev:ExtBlkRef = BlkPrevInfo 0;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: PrevBlkInfo
    ) = cellBuilder {
        storeTlb(ExtBlkRef, value.prev)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): PrevBlkInfo = cellSlice {
        val prev = loadTlb(ExtBlkRef)
        PrevBlkInfo(prev)
    }
}

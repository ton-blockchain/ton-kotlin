package org.ton.block

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.hashmap.HashmapAugE
import org.ton.kotlin.cell.CellContext
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbObject
import org.ton.tlb.TlbPrettyPrinter
import kotlin.jvm.JvmInline


@JvmInline
public value class OldMcBlocksInfo(
    public val value: HashmapAugE<KeyExtBlkRef, KeyMaxLt>
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return value.print(printer)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbCodec<OldMcBlocksInfo> by OldMcBlocksInfoTlbCodec
}

private object OldMcBlocksInfoTlbCodec : TlbCodec<OldMcBlocksInfo> {
    private val codec = HashmapAugE.tlbCodec(32, KeyExtBlkRef, KeyMaxLt)

    override fun storeTlb(builder: CellBuilder, value: OldMcBlocksInfo, context: CellContext) {
        codec.storeTlb(builder, value.value, context)
    }

    override fun loadTlb(slice: CellSlice, context: CellContext): OldMcBlocksInfo {
        return OldMcBlocksInfo(codec.loadTlb(slice, context))
    }
}

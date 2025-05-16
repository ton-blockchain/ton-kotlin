package org.ton.kotlin.block

import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.hashmap.HashMapE
import org.ton.kotlin.tlb.CellRef
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.TlbObject
import org.ton.kotlin.tlb.TlbPrettyPrinter
import kotlin.jvm.JvmInline


@JvmInline
public value class ShardHashes(
    public val value: HashMapE<CellRef<BinTree<ShardDescr>>>
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return value.print(printer)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbCodec<ShardHashes> by ShardHashesTlbCodec
}

private object ShardHashesTlbCodec : TlbCodec<ShardHashes> {
    private val codec = HashMapE.tlbCodec(32, CellRef.tlbCodec(BinTree.tlbCodec(ShardDescr)))
    override fun storeTlb(cellBuilder: CellBuilder, value: ShardHashes, context: CellContext) {
        codec.storeTlb(cellBuilder, value.value, context)
    }

    override fun loadTlb(cellSlice: CellSlice, context: CellContext): ShardHashes {
        return ShardHashes(codec.loadTlb(cellSlice, context))
    }
}

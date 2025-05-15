package org.ton.kotlin.tlb.providers

import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.TlbCombinator

public interface TlbCombinatorProvider<T : Any> : TlbProvider<T>, TlbCodec<T> {
    public fun tlbCombinator(): TlbCombinator<T>

    override fun loadTlb(slice: CellSlice, context: CellContext): T =
        tlbCombinator().loadTlb(slice, context)

    override fun storeTlb(builder: CellBuilder, value: T, context: CellContext): Unit =
        tlbCombinator().storeTlb(builder, value, context)
}

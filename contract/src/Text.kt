package org.ton.kotlin.contract

import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb

public data class Text(
    val data: SnakeData
) {
    public companion object : TlbConstructorProvider<Text> by TextConstructor
}

private object TextConstructor : TlbConstructor<Text>(
    schema = "text#_ {n:#} data:(SnakeData ~n) = Text;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: Text) {
        cellBuilder.storeTlb(SnakeData, value.data)
    }

    override fun loadTlb(cellSlice: CellSlice): Text = Text(cellSlice.loadTlb(SnakeData))
}

package org.ton.contract

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb

public data class Text(
    val data: SnakeData
) {
    public companion object : TlbConstructorProvider<Text> by TextConstructor
}

private object TextConstructor : TlbConstructor<Text>(
    schema = "text#_ {n:#} data:(SnakeData ~n) = Text;"
) {
    override fun storeTlb(builder: CellBuilder, value: Text) {
        builder.storeTlb(SnakeData, value.data)
    }

    override fun loadTlb(slice: CellSlice): Text = Text(slice.loadTlb(SnakeData))
}

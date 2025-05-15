package org.ton.kotlin.block

import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.storeTlb
import kotlin.test.assertEquals

fun <T> testSerialization(codec: TlbCodec<T>, stackValue: T) {
    val cellBuilder = CellBuilder.beginCell()
    cellBuilder.storeTlb(codec, stackValue)
    val cell = cellBuilder.endCell()

    val cellSlice = cell.beginParse()
    val result = cellSlice.loadTlb(codec)
    assertEquals(stackValue, result)
    cellSlice.endParse()
}

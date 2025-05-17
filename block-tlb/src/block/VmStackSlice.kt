package org.ton.kotlin.block

import org.ton.kotlin.cell.Cell
import org.ton.kotlin.cell.CellSlice

public sealed interface VmStackSlice : VmStackValue {
    public val cell: Cell
    public val stBits: Int
    public val endBits: Int
    public val stRef: Int
    public val endRef: Int

    public fun toCellSlice(): CellSlice = cell.beginParse().apply {
        bitsPosition += stBits
        refsPosition += stRef
    }
}

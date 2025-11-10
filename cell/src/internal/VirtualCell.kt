package org.ton.sdk.cell.internal

import org.ton.sdk.cell.Cell
import org.ton.sdk.cell.CellDescriptor
import org.ton.sdk.crypto.HashBytes

internal class VirtualCell(
    private val cellProvider: () -> Cell,
    private val offset: Int
) : Cell {
    constructor(cell: Cell, offset: Int) : this({ cell }, offset)

    override val descriptor: CellDescriptor get() = cellProvider().descriptor

    override fun virtualize(offset: Int): Cell {
        return if (this.offset == offset) this
        else VirtualCell(cellProvider, offset)
    }

    override fun hash(level: Int): HashBytes {
        return cellProvider().hash(descriptor.levelMask.virtualize(offset).apply(level).level)
    }

    override fun depth(level: Int): Int {
        return cellProvider().depth(descriptor.levelMask.virtualize(offset).apply(level).level)
    }
}

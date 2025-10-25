package org.ton.sdk.cell.internal

import org.ton.sdk.cell.Cell
import org.ton.sdk.cell.CellDescriptor
import org.ton.sdk.crypto.HashBytes

public class VirtualCell(
    private val cell: Cell,
    private val offset: Int
) : Cell {
    override val descriptor: CellDescriptor get() = cell.descriptor

    override fun virtualize(offset: Int): Cell {
        return if (this.offset == offset) this
        else VirtualCell(cell, offset)
    }

    override fun hash(level: Int): HashBytes {
        return cell.hash(descriptor.levelMask.apply(level).level)
    }

    override fun depth(level: Int): Int {
        return cell.depth(descriptor.levelMask.apply(level).level)
    }
}

package org.ton.sdk.cell.internal

import org.ton.sdk.cell.Cell
import org.ton.sdk.cell.CellDescriptor
import org.ton.sdk.crypto.HashBytes

internal class ExtCell(
    override val descriptor: CellDescriptor,
    val loader: CellLoader
) : Cell {
    val cell: Cell by lazy {
        loader.loadCell(this)
    }

    override fun virtualize(offset: Int): Cell = VirtualCell(this, offset)

    override fun hash(level: Int): HashBytes = cell.hash(level)

    override fun depth(level: Int): Int = cell.depth(level)
}

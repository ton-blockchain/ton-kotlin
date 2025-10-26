package org.ton.sdk.cell.internal

import org.ton.sdk.cell.Cell
import org.ton.sdk.cell.CellDescriptor
import org.ton.sdk.crypto.HashBytes

internal class LibraryCell(
    override val descriptor: CellDescriptor,
    val hash: HashBytes,
) : Cell {
    override fun virtualize(offset: Int): Cell = this

    override fun hash(level: Int): HashBytes = hash

    override fun depth(level: Int): Int = 0
}

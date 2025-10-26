package org.ton.sdk.cell.internal

import org.ton.sdk.cell.Cell
import org.ton.sdk.cell.CellDescriptor
import org.ton.sdk.crypto.HashBytes

internal class PrunedCell(
    override val descriptor: CellDescriptor,
    val hash: HashBytes,
    val data: ByteArray
) : Cell {
    override fun virtualize(offset: Int): Cell {
        TODO("Not yet implemented")
    }

    override fun hash(level: Int): HashBytes {
        val hashIndex = descriptor.levelMask.apply(level).hashIndex
        if (hashIndex == this.level) {
            return hash
        }

        TODO()
    }

    override fun depth(level: Int): Int {
        TODO("Not yet implemented")
    }
}

package org.ton.sdk.cell.internal

import org.ton.sdk.cell.Cell

internal fun interface CellLoader {
    fun loadCell(cell: Cell): Cell
}

package org.ton.sdk.cell.internal

import org.ton.sdk.cell.Cell
import org.ton.sdk.cell.CellContext
import org.ton.sdk.cell.LoadedCell

internal object EmptyCellContext : CellContext {
    override fun loadCell(cell: Cell): LoadedCell {
        if (cell is LoadedCell) return cell
        throw IllegalArgumentException("Can't load ${cell::class} $cell")
    }
}

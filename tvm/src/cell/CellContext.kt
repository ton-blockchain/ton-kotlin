@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.cell

public interface CellContext {
    public fun loadCell(cell: Cell): DataCell

    public fun finalizeCell(builder: CellBuilder): Cell

    public companion object {
        public val EMPTY: CellContext = object : CellContext {
            override fun loadCell(cell: Cell): DataCell {
                if (cell is DataCell) return cell
                if (cell is VirtualCell && cell.cell is DataCell) return cell.cell
                else throw IllegalArgumentException("Can't load ${cell::class} $cell")
            }

            override fun finalizeCell(builder: CellBuilder): Cell {
                return builder.build()
            }
        }
    }
}

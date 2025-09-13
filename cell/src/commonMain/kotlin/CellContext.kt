package org.ton.kotlin.cell

public interface CellContext {
    public fun loadCell(cell: Cell): DataCell

    public fun finalizeCell(cell: Cell): Cell

    public companion object {
        public val EMPTY: CellContext = object : CellContext {
            override fun loadCell(cell: Cell): DataCell {
                if (cell is DataCell) return cell
                throw IllegalArgumentException("Can't load $cell in empty context")
            }

            override fun finalizeCell(cell: Cell): DataCell {
                if (cell is DataCell) return cell
                throw IllegalArgumentException("Can't build $cell in empty context")
            }
        }
    }
}

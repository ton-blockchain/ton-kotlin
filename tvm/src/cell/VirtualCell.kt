package org.ton.kotlin.cell

import org.ton.kotlin.bitstring.BitString

internal class VirtualCell(
    val cell: Cell,
    val offset: Int
) : Cell by cell {
    override val refs: List<Cell>
        get() = cell.refs.map { it.virtualize(offset) }

    override val levelMask: LevelMask
        get() = cell.levelMask.virtualize(offset)

    override fun virtualize(offset: Int): Cell {
        return if (this.offset == offset) this
        else VirtualCell(cell, offset)
    }

    override fun hash(level: Int): BitString {
        return cell.hash(levelMask.apply(level).level)
    }

    override fun depth(level: Int): Int {
        return cell.depth(levelMask.apply(level).level)
    }

    override fun beginParse(): CellSlice {
        return CellSlice.beginParse(this)
    }
}

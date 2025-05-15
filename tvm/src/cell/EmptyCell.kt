package org.ton.kotlin.cell

import org.ton.kotlin.bitstring.BitString

internal object EmptyCell : Cell {
    private val EMPTY_CELL_HASH = BitString(
        "96a296d224f285c67bee93c30f8a309157f0daa35dc5b87e410b78630a09cfc7".hexToByteArray()
    )

    override val bits: BitString = BitString.empty()
    override val refs: List<Cell> = emptyList()
    override val descriptor: CellDescriptor = CellDescriptor(0, 0)

    override fun hash(level: Int): BitString = EMPTY_CELL_HASH

    override fun depth(level: Int): Int = 0

    override fun virtualize(offset: Int): Cell = this

    override fun toString(): String = "x{}"

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Cell) return false
        return descriptor == other.descriptor
    }

    override fun hashCode(): Int = 0
}

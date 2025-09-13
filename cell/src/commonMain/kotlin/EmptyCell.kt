package org.ton.kotlin.cell

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.hexToByteString

internal object EmptyCell : LoadedCell {
    private val EMPTY_CELL_HASH = "96a296d224f285c67bee93c30f8a309157f0daa35dc5b87e410b78630a09cfc7".hexToByteString()

    override val references: List<Cell>
        get() = emptyList()

    override val descriptor: CellDescriptor = CellDescriptor(0, 0)

    override fun hash(level: Int): ByteString = EMPTY_CELL_HASH

    override fun depth(level: Int): Int = 0

    override fun toString(): String = "x{}"

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Cell) return false
        return descriptor == other.descriptor
    }

    override fun hashCode(): Int = 0
}

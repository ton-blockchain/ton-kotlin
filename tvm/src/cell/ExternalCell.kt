@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.cell

import kotlinx.io.bytestring.ByteString
import org.ton.bitstring.BitString
import org.ton.cell.Cell
import org.ton.cell.CellDescriptor
import org.ton.cell.VirtualCell

public data class ExternalCell(
    override val descriptor: CellDescriptor,
    val hashes: List<ByteString>,
    val depths: List<Short>,
    val index: Int,
) : Cell {
    override fun hash(level: Int): BitString {
        val hashIndex = descriptor.levelMask.apply(level).hashIndex
        return BitString(hashes[hashIndex].toByteArray())
    }

    override fun depth(level: Int): Int {
        val hashIndex = descriptor.levelMask.apply(level).hashIndex
        return depths[hashIndex].toInt()
    }

    override fun virtualize(offset: Int): Cell {
        return if (levelMask.isEmpty()) {
            this
        } else {
            VirtualCell(this, offset)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        return "ExternalCell(index=$index, l=${descriptor.levelMask})"
    }
}

package org.ton.kotlin.cell

import kotlinx.io.bytestring.ByteString

public data class ExternalCell(
    override val descriptor: CellDescriptor,
    val hashes: List<ByteString>,
    val depths: List<Short>,
    val index: Int,
) : Cell {
    override fun hash(level: Int): ByteString {
        val hashIndex = descriptor.levelMask.apply(level).hashIndex
        return hashes[hashIndex]
    }

    override fun depth(level: Int): Int {
        val hashIndex = descriptor.levelMask.apply(level).hashIndex
        return depths[hashIndex].toInt()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        return "ExternalCell(index=$index, l=${descriptor.levelMask})"
    }
}

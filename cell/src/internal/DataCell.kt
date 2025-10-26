package org.ton.sdk.cell.internal

import org.ton.sdk.bitstring.BitString
import org.ton.sdk.cell.Cell
import org.ton.sdk.cell.CellDescriptor
import org.ton.sdk.cell.LoadedCell
import org.ton.sdk.crypto.HashBytes

internal class DataCell(
    override val descriptor: CellDescriptor,
    val bits: BitString,
    private val references: List<Cell>,
    private val hashes: Array<HashBytes>,
    private val depths: IntArray
) : LoadedCell {
    private var hashCode: Int = 0

    override fun reference(index: Int): Cell? = references.getOrNull(index)

    override fun virtualize(offset: Int): Cell {
        return if (levelMask.isEmpty()) {
            this
        } else {
            VirtualCell(this, offset)
        }
    }

    override fun hash(level: Int): HashBytes {
        val hashIndex = levelMask.apply(level).hashIndex
        return hashes[hashIndex]
    }

    override fun depth(level: Int): Int {
        val depthIndex = levelMask.apply(level).hashIndex
        return depths[depthIndex]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataCell) return false
        if (hashCode != 0 && other.hashCode != 0 && hashCode != other.hashCode) return false
        return hash() == other.hash()
    }

    override fun hashCode(): Int {
        var hc = hashCode
        if (hc == 0) {
            hc = hash().hashCode()
            hashCode = hc
        }
        return hc
    }

    override fun toString(): String = "DataCell(d=${descriptor},$bits)"
}

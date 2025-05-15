package org.ton.kotlin.cell

import org.ton.kotlin.bitstring.BitString

public class DataCell(
    override val descriptor: CellDescriptor,
    override val bits: BitString,
    override val refs: List<Cell>,
    internal val hashes: List<Pair<ByteArray, Int>>
) : Cell {
    private val hashCode: Int by lazy(LazyThreadSafetyMode.PUBLICATION) {
        var result = descriptor.hashCode()
        result = 31 * result + hashes.hashCode()
        result
    }

    override fun hash(level: Int): BitString {
        val hashIndex = levelMask.apply(level).hashIndex
        return BitString(hashes[hashIndex].first)
    }

    override fun depth(level: Int): Int {
        val hashIndex = levelMask.apply(level).hashIndex
        return hashes[hashIndex].second
    }

    override fun virtualize(offset: Int): Cell {
        return if (levelMask.isEmpty()) {
            this
        } else {
            VirtualCell(this, offset)
        }
    }

    override fun toString(): String = Cell.toString(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataCell) return false

        if (descriptor != other.descriptor) return false
        if (bits != other.bits) return false
        return refs == other.refs
    }

    override fun hashCode(): Int = hashCode
}

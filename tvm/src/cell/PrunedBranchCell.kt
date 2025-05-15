package org.ton.kotlin.cell

import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.bitstring.toBitString

public class PrunedBranchCell(
    private val hash: BitString,
    private val depth: Int,
    override val descriptor: CellDescriptor,
    override val bits: BitString
) : Cell {
    override val refs: List<Cell> get() = emptyList()

    override fun hash(level: Int): BitString {
        val hashIndex = descriptor.levelMask.apply(level).hashIndex
        return if (hashIndex == descriptor.levelMask.level) {
            hash
        } else {
            val offset = 2 + hashIndex * 32
            bits.toByteArray().copyOfRange(offset, offset + 32).toBitString()
        }
    }

    override fun depth(level: Int): Int {
        val hashIndex = descriptor.levelMask.apply(level).hashIndex
        return if (hashIndex == descriptor.levelMask.level) {
            depth
        } else {
            val offset = 2 + descriptor.levelMask.level * 32 + hashIndex * 2
            val data = bits.toByteArray()
            ((data[offset].toInt() and 0xFF) shl 8) or (data[offset + 1].toInt() and 0xFF)
        }
    }

    override fun virtualize(offset: Int): Cell =
        VirtualCell(this, offset)

    override fun toString(): String = "$type x{$bits}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PrunedBranchCell) return false
        return hash == other.hash
    }

    override fun hashCode(): Int = hash.hashCode()
}

package org.ton.kotlin.cell

import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.bitstring.BitString

public data class DataCell(
    override val descriptor: CellDescriptor,
    private val hashes: List<ByteString>,
    private val depths: List<Short>,
    val bits: BitString,
    override val references: List<Cell>
) : LoadedCell {
    private var hashCode: Int = 0

    override fun hash(level: Int): ByteString {
        var index = descriptor.levelMask.apply(level).hashIndex
        if (cellType == CellType.PRUNED_BRANCH) {
            if (index != descriptor.levelMask.hashIndex) {
                val offset = 2 + index * 32
                val bytes = bits.toByteArray()
                return ByteString(bytes, offset, offset + 32)
            }
            index = 0
        }
        return hashes[index]
    }

    override fun depth(level: Int): Int {
        var index = descriptor.levelMask.apply(level).hashIndex
        if (cellType == CellType.PRUNED_BRANCH) {
            TODO()
        }
        return depths[index].toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DataCell

        if (hashCode != 0 && other.hashCode != 0 && hashCode != other.hashCode) return false
        if (descriptor != other.descriptor) return false
        if (depths != other.depths) return false
        if (hashes != other.hashes) return false
        if (bits != other.bits) return false
        if (references != other.references) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hashCode
        if (result == 0) {
            result = 31 * result + descriptor.hashCode()
            result = 31 * result + hashes.hashCode()
            result = 31 * result + depths.hashCode()
            result = 31 * result + bits.hashCode()
            result = 31 * result + references.hashCode()
            hashCode = result
        }
        return result
    }
}

package org.ton.sdk.cell.internal

import kotlinx.io.bytestring.unsafe.UnsafeByteStringApi
import org.ton.sdk.bitstring.unsafe.UnsafeBitStringApi
import org.ton.sdk.cell.Cell
import org.ton.sdk.cell.CellDescriptor
import org.ton.sdk.crypto.HashBytes
import kotlin.math.min

@OptIn(UnsafeByteStringApi::class, UnsafeBitStringApi::class)
internal class PrunedCell(
    override val descriptor: CellDescriptor,
    val hashes: Array<HashBytes>,
    val depths: IntArray
) : Cell {
    private var hashCode: Int = 0

    override fun virtualize(offset: Int): Cell = VirtualCell(this, offset)

    override fun hash(level: Int): HashBytes {
        return hashes[min(level, descriptor.levelMask.level)]
    }

    override fun depth(level: Int): Int {
        return depths[min(level, descriptor.levelMask.level)]
    }

    override fun toString(): String {
        return "PrunedCell(descriptor=$descriptor, hash=${hashes[0]})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PrunedCell

        if (descriptor != other.descriptor) return false
        if (hashCode != 0 && other.hashCode != 0 && hashCode != other.hashCode) return false
        if (hash() != other.hash()) return false

        return true
    }

    override fun hashCode(): Int {
        var hc = hashCode
        if (hc == 0) {
            hc = hash().hashCode()
            hashCode = hc
        }
        return hc
    }
}

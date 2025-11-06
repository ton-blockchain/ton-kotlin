package org.ton.sdk.cell.internal

import kotlinx.io.bytestring.unsafe.UnsafeByteStringApi
import kotlinx.io.bytestring.unsafe.UnsafeByteStringOperations
import org.ton.sdk.bitstring.BitString
import org.ton.sdk.bitstring.unsafe.UnsafeBitStringApi
import org.ton.sdk.bitstring.unsafe.UnsafeBitStringOperations
import org.ton.sdk.cell.Cell
import org.ton.sdk.cell.CellDescriptor
import org.ton.sdk.crypto.HashBytes

internal class PrunedCell(
    override val descriptor: CellDescriptor,
    val hash: HashBytes,
    val data: BitString
) : Cell {
    override fun virtualize(offset: Int): Cell = VirtualCell(this, offset)

    @OptIn(UnsafeByteStringApi::class, UnsafeBitStringApi::class)
    override fun hash(level: Int): HashBytes {
        val hashIndex = descriptor.levelMask.apply(level).hashIndex
        val cellLevel = this.level
        if (hashIndex == cellLevel) {
            return hash
        }
        val offset = 2 + hashIndex * 32
        return UnsafeBitStringOperations.withByteArrayUnsafe(data) {
            HashBytes(
                UnsafeByteStringOperations.wrapUnsafe(it.copyOfRange(offset, offset + 32))
            )
        }
    }

    @Suppress("OPT_IN_USAGE")
    override fun depth(level: Int): Int {
        val hashIndex = descriptor.levelMask.apply(level).hashIndex
        val cellLevel = this.level
        if (hashIndex == cellLevel) {
            return 0
        }
        val offset = 2 + cellLevel * 32 + hashIndex * 2
        return UnsafeBitStringOperations.withByteArrayUnsafe(data) {
            (it[offset].toInt() and 0xFF shl 8) or
                    it[offset + 1].toInt() and 0xFF
        }
    }

    override fun toString(): String {
        return "PrunedCell(descriptor=$descriptor, hash=$hash, data=$data)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PrunedCell

        if (descriptor != other.descriptor) return false
        if (hash != other.hash) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = descriptor.hashCode()
        result = 31 * result + hash.hashCode()
        result = 31 * result + data.hashCode()
        return result
    }
}

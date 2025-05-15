@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.dict

import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.bitstring.ByteBackedBitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.storeUInt

internal fun CellBuilder.storeLabel(
    maxKeyLength: Int,
    key: BitString,
    startIndex: Int,
    endIndex: Int
) {
    val length = endIndex - startIndex
    if (length == 0) {
        // mode 'hml_short$0', requires 2n+2 bits (always for n=0)
        storeUInt(0u, 2)
        return
    }
    val k = Int.SIZE_BITS - maxKeyLength.countLeadingZeroBits()

    val firstBit = key[startIndex]
    when {
        length > 1 && k < 2 * length - 1 && length == key.countLeadingBits(startIndex, endIndex, firstBit) -> {
            // mode 'hml_same$11', requires 3+k bits (for n>=2, k<2n-1)
            storeUInt(if (firstBit) 0b111u else 0b110u, 3)
            storeUInt(length.toUInt(), k)
            return
        }

        k < length -> {
            // mode 'hml_long$10', requires 2+k+n bits
            storeUInt(0b10u, 2) // tag hml_long$10
            storeUInt(length.toUInt(), k) // 'n:(#<= m)' from hml_long
        }

        else -> {
            // mode 'hml_short$0', requires 1+2n+1 bits
            storeBoolean(false) // tag hml_short$0
            // 'len:(Unary ~n)' from hml_short,
            // where -0b10 = 0b1111_1111_1111_1110,
            // unary_succ$1 - counter for n
            // unary_zero$0 - terminator for counter
            // 1110 -> n=3 + 1 terminator bit
            storeLong(-0b10, length + 1)
        }
    }
    storeBitString(key, startIndex, endIndex)
}

internal fun CellSlice.readLabel(keyBitLength: Int): BitString {
    val labelType = preloadUInt(2).toInt()
    when (labelType) {
        // hml_short$0 unary_zero$0
        0b00 -> {
            skipBits(2)
            return BitString.empty()
        }
        // hml_short$0 unary_succ$1
        0b01 -> {
            skipBits(1)
            val len = countLeadingBits(bit = true)
            skipBits(len + 1)
            return loadBitString(len)
        }
        // hml_long$10
        0b10 -> {
            skipBits(2)
            val len = loadUIntLeq(keyBitLength).toInt()
            return loadBitString(len)
        }
        // hml_same$11
        0b11 -> {
            skipBits(2)
            val sameBit = loadBoolean()
            val len = loadUIntLeq(keyBitLength).toInt()
            val bytes = ByteArray((len + 7) ushr 3)
            if (sameBit) {
                bytes.fill(0xFF.toByte())
            }
            return ByteBackedBitString.of(bytes, len)
        }

        else -> throw IllegalArgumentException("Invalid label type: $labelType")
    }
}

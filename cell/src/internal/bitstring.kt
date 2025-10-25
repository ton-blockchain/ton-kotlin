package org.ton.sdk.cell.internal

internal fun storeLong(
    dest: ByteArray,
    destOffset: Int,
    bitOffset: Int,
    value: Long,
    bits: Int,
) {
    require(bitOffset in 0..7)
    require(bits in 0..64)
    if (bits == 0) return

    var topBits = bits
    var v = value.toULong()
    if (topBits < 64) {
        v = v shl (64 - topBits)
    }

    // Fast path: byte-aligned start and whole bytes length
    if (bitOffset == 0 && (topBits and 7) == 0) {
        val byteLen = topBits ushr 3
        var i = 0
        while (i < byteLen) {
            dest[destOffset + i] = (v shr (56 - 8 * i)).toByte()
            i++
        }
        return
    }

    // Build 64-bit window z that combines preserved high bits of the first byte and shifted v
    val maskFirst = ((0xFF shl (8 - bitOffset)) and 0xFF)
    val preservedHighFirst = dest[destOffset].toInt() and maskFirst
    val z = ((preservedHighFirst.toULong()) shl 56) or (v shr bitOffset)

    // After merging first byte, account for initial bit offset
    topBits += bitOffset

    if (topBits > 64) {
        // Spill into 9th byte
        writeLongBE(dest, destOffset, z.toLong())

        val tailBits = topBits - 64 // in 1..7
        val z2 = v shl (8 - bitOffset)
        val maskTail = 0xFF ushr tailBits
        val invMaskTail = maskTail.inv() and 0xFF

        val oldByte = dest[destOffset + 8].toInt() and 0xFF
        val newByte = (oldByte and maskTail) or (((z2 shr 56).toInt() and 0xFF) and invMaskTail)
        dest[destOffset + 8] = newByte.toByte()
        return
    } else {
        // Everything fits within 64 bits from the start of the first touched byte
        var p = 56
        val q = 64 - topBits // 0..56
        var curIdx = destOffset

        if (q <= 32) {
            writeIntBE(dest, curIdx, (z shr 32).toInt())
            curIdx += 4
            p -= 32
        }
        while (p >= q) {
            dest[curIdx++] = (z shr p).toByte()
            p -= 8
        }
        val remainingOverwriteBits = p + 8 - q
        if (remainingOverwriteBits > 0) {
            val mask = 0xFF ushr remainingOverwriteBits
            val invMask = mask.inv() and 0xFF
            val oldByte = dest[curIdx].toInt() and 0xFF
            val newTop = ((z shr p).toInt() and 0xFF) and invMask
            dest[curIdx] = ((oldByte and mask) or newTop).toByte()
        }
        return
    }
}

/** Writes a 64-bit integer in big-endian order. */
@Suppress("NOTHING_TO_INLINE")
private inline fun writeLongBE(dest: ByteArray, destOffset: Int, v: Long) {
    dest[destOffset + 0] = (v ushr 56).toByte()
    dest[destOffset + 1] = (v ushr 48).toByte()
    dest[destOffset + 2] = (v ushr 40).toByte()
    dest[destOffset + 3] = (v ushr 32).toByte()
    dest[destOffset + 4] = (v ushr 24).toByte()
    dest[destOffset + 5] = (v ushr 16).toByte()
    dest[destOffset + 6] = (v ushr 8).toByte()
    dest[destOffset + 7] = (v ushr 0).toByte()
}

/** Writes a 32-bit integer in big-endian order. */
@Suppress("NOTHING_TO_INLINE")
private inline fun writeIntBE(dest: ByteArray, destOffset: Int, v: Int) {
    dest[destOffset + 0] = (v ushr 24).toByte()
    dest[destOffset + 1] = (v ushr 16).toByte()
    dest[destOffset + 2] = (v ushr 8).toByte()
    dest[destOffset + 3] = (v ushr 0).toByte()
}

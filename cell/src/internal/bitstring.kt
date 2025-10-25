package org.ton.sdk.cell.internal

/**
 * Writes up to 64 bits of the given value into the destination bitstring.
 *
 * Semantics
 * - Writing is MSB-first inside a byte (bit masks go 0x80, 0x40, ..., 0x01).
 * - The first written bit goes to `dest[destOffset]` at position specified by [bitOffset].
 * - [bitOffset] is in 0..7 and counts how many highest bits of dest[destOffset] must be preserved.
 *   For example, bitOffset=0 means we start at the MSB (0x80) of dest[destOffset];
 *   bitOffset=3 means the top three bits are kept as-is and we start at mask 0x10.
 * - Only the top [bits] of [value] are stored. If [bits] < 64, the value is logically left-shifted
 *   so that the first bit to store resides at bit 63 of a 64-bit register. Negative values are
 *   handled naturally via two's complement representation.
 * - Bits outside the target range are preserved: the high bits before [bitOffset] in the first byte
 *   and the low bits after the last written bit in the final byte are not modified.
 *
 * Examples
 * - storeLong(dest, 0, 0, 0xAB, 8) -> writes 0xAB at dest[0].
 * - storeLong(dest, 0, 1, 0xFF, 8) with zeroed dest -> dest = [0x7F, 0x80, ...].
 *
 * This implementation mirrors `td::bitstring::bits_store_long_top` from the TON C++ reference.
 */
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

    // Normalize: take only the top `bits` of value and move them to the highest bits.
    // If bits < 64, shift left so that the first bit to store is at position 63.
    var topBits = bits
    var v = value.toULong()
    if (topBits < 64) {
        v = v shl (64 - topBits)
    }

    // Fast path: byte-aligned start and length is a multiple of whole bytes.
    // In this case we can just dump consecutive bytes in big-endian order.
    if (bitOffset == 0 && (topBits and 7) == 0) {
        val byteLen = topBits ushr 3
        var i = 0
        while (i < byteLen) {
            // Take successive bytes from MSB to LSB of `v`.
            dest[destOffset + i] = (v shr (56 - 8 * i)).toByte()
            i++
        }
        return
    }

    // Unaligned path.
    // Build a 64-bit window `z` that combines:
    // - preserved high bits of the first destination byte (above bitOffset), and
    // - the bits of `v` shifted right by bitOffset so that the first bit of `v`
    //   lands immediately after the preserved bits.
    val maskFirst = ((0xFF shl (8 - bitOffset)) and 0xFF) // high `bitOffset` bits set
    val preservedHighFirst = dest[destOffset].toInt() and maskFirst
    val z = ((preservedHighFirst.toULong()) shl 56) or (v shr bitOffset)

    // From the beginning of the first touched byte, we now need to consider that
    // `bitOffset` bits are part of the window as well (they are preserved).
    topBits += bitOffset

    if (topBits > 64) {
        // The written region spans beyond 8 bytes (spills into the 9th byte).
        // Write the first 8 bytes from `z` and then merge the tail bits into dest[destOffset + 8].
        writeLongBE(dest, destOffset, z.toLong())

        val tailBits = topBits - 64 // number of bits to overwrite in the 9th byte, in 1..7
        // Align the remainder so that its top 8 bits will form the byte to merge.
        val z2 = v shl (8 - bitOffset)
        // Preserve the low (8 - tailBits) bits of the 9th byte; overwrite only the top `tailBits` bits.
        val maskTail = 0xFF ushr tailBits
        val invMaskTail = maskTail.inv() and 0xFF

        val oldByte = dest[destOffset + 8].toInt() and 0xFF
        val newByte = (oldByte and maskTail) or (((z2 shr 56).toInt() and 0xFF) and invMaskTail)
        dest[destOffset + 8] = newByte.toByte()
        return
    } else {
        // Everything fits within the 64-bit window starting at the first touched byte.
        // We will stream bytes from `z` starting at position `p` down to the lowest byte we need (>= q).
        var p = 56
        val q = 64 - topBits // minimal bit position in `z` we must touch (0..56)
        var curIdx = destOffset

        // If the region includes the top 32 bits of `z`, write them quickly as a u32.
        if (q <= 32) {
            writeIntBE(dest, curIdx, (z shr 32).toInt())
            curIdx += 4
            p -= 32
        }
        // Write full bytes while the next byte is fully inside the required region.
        while (p >= q) {
            dest[curIdx++] = (z shr p).toByte()
            p -= 8
        }
        // If the last byte is only partially overwritten, merge it with a mask to preserve lower bits.
        val remainingOverwriteBits = p + 8 - q
        if (remainingOverwriteBits > 0) {
            val mask = 0xFF ushr remainingOverwriteBits   // keeps the low, not-overwritten bits
            val invMask = mask.inv() and 0xFF             // selects the high, to-be-written bits
            val oldByte = dest[curIdx].toInt() and 0xFF
            val newTop = ((z shr p).toInt() and 0xFF) and invMask
            dest[curIdx] = ((oldByte and mask) or newTop).toByte()
        }
        return
    }
}

/** Writes a 64-bit integer to dest in big-endian (network) byte order. */
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

/** Writes a 32-bit integer to dest in big-endian (network) byte order. */
@Suppress("NOTHING_TO_INLINE")
private inline fun writeIntBE(dest: ByteArray, destOffset: Int, v: Int) {
    dest[destOffset + 0] = (v ushr 24).toByte()
    dest[destOffset + 1] = (v ushr 16).toByte()
    dest[destOffset + 2] = (v ushr 8).toByte()
    dest[destOffset + 3] = (v ushr 0).toByte()
}

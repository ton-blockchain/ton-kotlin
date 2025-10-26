package org.ton.sdk.bitstring.internal

import org.ton.sdk.bigint.*
import kotlin.math.min

internal fun bitsCopy(
    dest: ByteArray,
    destBitOffset: Int,
    src: ByteArray,
    srcBitOffset: Int,
    bitCount: Int
) {
    if (bitCount <= 0) {
        return
    }

    var fromIdx = srcBitOffset shr 3
    var toIdx = destBitOffset shr 3
    var fromOffs = srcBitOffset and 7
    var toOffs = destBitOffset and 7

    val sz = bitCount
    var totalBits = bitCount + fromOffs

    if (fromOffs == toOffs) {
        // Fast path: same bit offset in both arrays
        if (totalBits < 8) {
            // Less than a byte to copy
            val mask = ((-0x100 shr totalBits) and (0xff shr toOffs))
            dest[toIdx] = ((dest[toIdx].toInt() and mask.inv()) or (src[fromIdx].toInt() and mask)).toByte()
            return
        }

        val l = totalBits shr 3
        if (toOffs == 0) {
            // Byte-aligned copy
            src.copyInto(dest, toIdx, fromIdx, fromIdx + l)
        } else {
            // Copy first partial byte
            val mask = (0xff shr toOffs)
            dest[toIdx] = ((dest[toIdx].toInt() and mask.inv()) or (src[fromIdx].toInt() and mask)).toByte()
            // Copy full bytes
            src.copyInto(dest, toIdx + 1, fromIdx + 1, fromIdx + l)
        }

        // Copy remaining bits
        val remainingBits = totalBits and 7
        if (remainingBits != 0) {
            val mask = (-0x100 shr remainingBits)
            dest[toIdx + l] = ((dest[toIdx + l].toInt() and mask.inv()) or (src[fromIdx + l].toInt() and mask)).toByte()
        }
    } else {
        // General path: different bit offsets
        var b = toOffs
        var acc = if (b != 0) ((dest[toIdx].toInt() and 0xFF) ushr (8 - b)).toLong() else 0L

        if (totalBits < 8) {
            acc = acc shl sz
            acc = acc or (((src[fromIdx].toInt() and 0xFF) and (0xff shr fromOffs)) ushr (8 - totalBits)).toLong()
            b += sz
        } else {
            val ld = 8 - fromOffs
            acc = acc shl ld
            acc = acc or ((src[fromIdx++].toInt() and 0xFF) and (0xff shr fromOffs)).toLong()
            b += ld
            totalBits -= 8

            // Copy 32-bit blocks
            while (totalBits >= 32) {
                acc = acc shl 32
                val word = ((src[fromIdx].toInt() and 0xFF) shl 24) or
                        ((src[fromIdx + 1].toInt() and 0xFF) shl 16) or
                        ((src[fromIdx + 2].toInt() and 0xFF) shl 8) or
                        (src[fromIdx + 3].toInt() and 0xFF)
                acc = acc or word.toLong()
                fromIdx += 4

                val outWord = (acc ushr b).toInt()
                dest[toIdx] = (outWord ushr 24).toByte()
                dest[toIdx + 1] = (outWord ushr 16).toByte()
                dest[toIdx + 2] = (outWord ushr 8).toByte()
                dest[toIdx + 3] = outWord.toByte()
                toIdx += 4
                totalBits -= 32
            }

            // Copy remaining 8-bit blocks
            while (totalBits >= 8) {
                acc = acc shl 8
                acc = acc or (src[fromIdx++].toInt() and 0xFF).toLong()
                totalBits -= 8
                b += 8
            }

            // Copy remaining bits
            if (totalBits > 0) {
                acc = acc shl totalBits
                acc = acc or ((src[fromIdx].toInt() and 0xFF) ushr (8 - totalBits)).toLong()
                b += totalBits
            }
        }

        // Write out accumulated bits
        while (b >= 8) {
            b -= 8
            dest[toIdx++] = (acc ushr b).toByte()
        }

        if (b > 0) {
            dest[toIdx] = ((dest[toIdx].toInt() and (0xff shr b)) or ((acc shl (8 - b)).toInt() and 0xFF)).toByte()
        }
    }
}

@PublishedApi
internal fun bitsCompare(
    a: ByteArray,
    b: ByteArray,
    bitCount: Int
): Int {
    if (bitCount <= 0) return 0

    var ai = 0
    var bi = 0
    var bits = bitCount

    // Compare full 32-bit blocks
    while (bits >= 32) {
        val va =
            (a[ai].toInt() and 0xFF shl 24) or
                    (a[ai + 1].toInt() and 0xFF shl 16) or
                    (a[ai + 2].toInt() and 0xFF shl 8) or
                    (a[ai + 3].toInt() and 0xFF)
        val vb =
            (b[bi].toInt() and 0xFF shl 24) or
                    (b[bi + 1].toInt() and 0xFF shl 16) or
                    (b[bi + 2].toInt() and 0xFF shl 8) or
                    (b[bi + 3].toInt() and 0xFF)

        if (va != vb) return ucmp(va, vb)
        ai += 4; bi += 4
        bits -= 32
    }

    // Remainder [1..31] bits: read next up to 4 bytes and mask top bits
    if (bits > 0) {
        val va = readWin32MSB(a, ai) // окно MSB-first
        val vb = readWin32MSB(b, bi)
        val mask = -1 shl (32 - bits)
        val da = va and mask
        val db = vb and mask
        if (da != db) return ucmp(da, db)
    }
    return 0
}

// Читает до 4 байт и формирует MSB-окно: байт[i] в старшие биты.
@Suppress("NOTHING_TO_INLINE")
private inline fun readWin32MSB(src: ByteArray, i: Int): Int {
    var v = 0
    if (i < src.size) v = v or ((src[i].toInt() and 0xFF) shl 24)
    if (i + 1 < src.size) v = v or ((src[i + 1].toInt() and 0xFF) shl 16)
    if (i + 2 < src.size) v = v or ((src[i + 2].toInt() and 0xFF) shl 8)
    if (i + 3 < src.size) v = v or ((src[i + 3].toInt() and 0xFF) shl 0)
    return v
}

// Unsigned Int compare
@Suppress("NOTHING_TO_INLINE")
private inline fun ucmp(a: Int, b: Int): Int {
    val x = a xor Int.MIN_VALUE
    val y = b xor Int.MIN_VALUE
    return if (x < y) -1 else if (x > y) 1 else 0
}

private val HEX_DIGITS = charArrayOf(
    '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
)

internal fun bitsToHex(
    sb: Appendable,
    src: ByteArray,
    srcOffset: Int,
    bitOffset: Int,
    bitCount: Int,
) {
    if (bitCount == 0) {
        return
    }

    var ptr = srcOffset + (bitOffset shr 3)
    var offs = bitOffset and 7
    var acc = (src[ptr++].toInt() and 0xFF) and (0xFF shr offs)
    var bits = 8 - offs

    var len = bitCount

    if (bits > len) {
        acc = acc shr (bits - len)
        bits = len
    } else {
        len -= bits

        while (len >= 8) {
            while (len >= 8 && bits <= 24) {
                acc = acc shl 8
                acc = acc or (src[ptr++].toInt() and 0xFF)
                bits += 8
                len -= 8
            }

            while (bits >= 4) {
                bits -= 4
                sb.append(HEX_DIGITS[(acc ushr bits) and 15])
            }
        }

        if (len > 0) {
            acc = acc shl len
            acc = acc or ((src[ptr].toInt() and 0xFF) shr (8 - len))
            bits += len
        }
    }

    val f = bits and 3
    if (f != 0) {
        acc = (2 * acc + 1) shl (3 - f)
        bits += 4 - f
    }

    while (bits >= 4) {
        bits -= 4
        sb.append(HEX_DIGITS[(acc shr bits) and 15])
    }

    check(bits == 0)

    if (f != 0) {
        sb.append('_')
    }
}

internal fun countLeadingBits(
    array: ByteArray,
    offset: Int,
    bitCount: Int,
    bit: Boolean
): Int {
    if (bitCount == 0) return 0

    val xorVal = if (bit) -1 else 0
    var index = offset ushr 3
    val bitOffset = offset and 7
    var reminder = bitCount

    if (bitOffset != 0) {
        val v = ((array[index++].toInt() and 0xFF) xor xorVal) shl (24 + bitOffset)
        val c = v.countLeadingZeroBits()
        val remainingBits = 8 - bitOffset
        if (c < remainingBits || bitCount <= remainingBits) {
            return min(c, bitCount)
        }
        reminder -= remainingBits
    }

    while (reminder >= 8) {
        val v = ((array[index++].toInt() xor xorVal) and 0xFF) shl 24
        if (v != 0) {
            return bitCount - reminder + v.countLeadingZeroBits()
        }
        reminder -= 8
    }

    if (reminder > 0) {
        val v = (((array[index].toInt() xor xorVal) and 0xFF) shl 24).countLeadingZeroBits()
        if (v < reminder) {
            return bitCount - reminder + v
        }
    }
    return bitCount
}

internal fun bitsParseHex(
    dst: ByteArray,
    dstOffset: Int,
    src: CharSequence,
    startIndex: Int,
    endIndex: Int
): Int {
    var hexDigitsCount = 0
    var cmpl = false
    var dstIndex = dstOffset ushr 3
    var bits = 0

    var i = startIndex
    while (i < endIndex) {
        val c = src[i++]
        when {
            cmpl -> return bits
            c in '0'..'9' || c in 'a'..'f' || c in 'A'..'F' -> {
                val v = when (c) {
                    in '0'..'9' -> c - '0'
                    in 'a'..'f' -> (c - 'a') + 10
                    else -> (c - 'A') + 10
                }
                if (hexDigitsCount >= dst.size * 2) {
                    return bits
                }
                if (hexDigitsCount and 1 == 0) {
                    dst[dstIndex] = (v shl 4).toByte()
                } else {
                    dst[dstIndex] = (dst[dstIndex].toInt() or v).toByte()
                    dstIndex++
                }
                hexDigitsCount++
                bits += 4
            }

            c == '_' -> {
                cmpl = true
            }

            else -> return bits
        }
    }

    if (cmpl && bits > 0) {
        var t = if (hexDigitsCount and 1 != 0) {
            (0x100 + (dst[dstIndex].toInt() and 0xFF)) shr 4
        } else {
            (0x100 + (dst[--dstIndex].toInt() and 0xFF))
        }
        while (bits > 0) {
            if (t == 1) {
                t = 0x100 + (dst[--dstIndex].toInt() and 0xFF)
            }
            bits--
            if (t and 1 != 0) {
                break
            }
            t = t shr 1
        }
    }

    return bits
}

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
internal fun storeLongIntoByteArray(
    dest: ByteArray,
    destOffset: Int,
    bitOffset: Int,
    value: Long,
    bits: Int,
) {
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

/** Writes up to 32 bits of the given Int value into the destination bitstring.
 * Semantics are identical to [storeLongIntoByteArray], but limited to 0..32 bits.
 *
 * This version avoids using 64-bit arithmetic for platforms where `Long` is slow or unavailable.
 */
internal fun storeIntIntoByteArray(
    dest: ByteArray,
    destOffset: Int,
    bitOffset: Int,
    value: Int,
    bits: Int,
) {
    if (bits == 0) return

    var topBits = bits
    var v = value
    if (topBits < 32) {
        // Move the first bit to store to bit 31 (MSB) of a 32-bit register.
        v = v shl (32 - topBits)
    }

    // Fast path: byte-aligned start and length is a multiple of whole bytes.
    if (bitOffset == 0 && (topBits and 7) == 0) {
        val byteLen = topBits ushr 3
        var i = 0
        while (i < byteLen) {
            dest[destOffset + i] = ((v ushr (24 - 8 * i)) and 0xFF).toByte()
            i++
        }
        return
    }

    // Unaligned path: build a 32-bit window `z` that starts at the first touched byte.
    val maskFirst = ((0xFF shl (8 - bitOffset)) and 0xFF) // high `bitOffset` bits set
    val preservedHighFirst = dest[destOffset].toInt() and maskFirst
    val z = (preservedHighFirst shl 24) or (v ushr bitOffset)

    // Account for the preserved high bits being part of the window.
    topBits += bitOffset

    if (topBits > 32) {
        // Region spills into the 5th byte.
        writeIntBE(dest, destOffset, z)

        val tailBits = topBits - 32 // number of bits to overwrite in the 5th byte, in 1..7
        // Align the remainder so that its top 8 bits will form the byte to merge.
        val z2 = v shl (8 - bitOffset)
        // Preserve the low (8 - tailBits) bits of the 5th byte; overwrite only the top `tailBits` bits.
        val maskTail = 0xFF ushr tailBits
        val invMaskTail = maskTail.inv() and 0xFF

        val oldByte = dest[destOffset + 4].toInt() and 0xFF
        val newByte = (oldByte and maskTail) or (((z2 ushr 24) and 0xFF) and invMaskTail)
        dest[destOffset + 4] = newByte.toByte()
        return
    } else {
        // Everything fits within the 32-bit window `z`.
        var p = 24
        val q = 32 - topBits // minimal bit position in `z` we must touch (0..24)
        var curIdx = destOffset

        // Stream full bytes while they are fully inside the required region.
        while (p >= q + 8) {
            dest[curIdx++] = ((z ushr p) and 0xFF).toByte()
            p -= 8
        }
        // If the next byte is fully inside, write it too (matches the original algorithm semantics).
        while (p >= q) {
            dest[curIdx++] = ((z ushr p) and 0xFF).toByte()
            p -= 8
        }
        // If the last byte is only partially overwritten, merge it with a mask to preserve lower bits.
        val remainingOverwriteBits = p + 8 - q
        if (remainingOverwriteBits > 0) {
            val mask = 0xFF ushr remainingOverwriteBits   // keeps the low, not-overwritten bits
            val invMask = mask.inv() and 0xFF             // selects the high, to-be-written bits
            val oldByte = dest[curIdx].toInt() and 0xFF
            val newTop = ((z ushr p) and 0xFF) and invMask
            dest[curIdx] = ((oldByte and mask) or newTop).toByte()
        }
        return
    }
}

internal fun storeBigIntIntoByteArray(
    dest: ByteArray,
    destOffset: Int,
    bitOffset: Int,
    value: BigInt,
    bits: Int,
) {
    if (bits == 0) return

    // Small-width fast paths delegate to the 64-bit implementation for exact parity with storeLong
    if (bits <= 64) {
        val maskBits64 = if (bits == 64) null else ((1.toBigInt() shl bits) - 1.toBigInt())
        val v64 = if (maskBits64 != null) (value and maskBits64).toLong() else value.toLong()
        storeLongIntoByteArray(dest, destOffset, bitOffset, v64, bits)
        return
    }

    // General path for arbitrary bit length using streaming BigInt operations.
    // We construct a window of K bytes that covers [bitOffset + bits] bits starting
    // from the first touched byte. We left-shift the VALUE'S LOWER `bits` so that the first payload bit
    // lands right after the preserved high `bitOffset` bits of that window.
    val totalBits = bitOffset + bits
    val kBytes = (totalBits + 7) ushr 3 // ceil(totalBits / 8)

    // Take only the lowest `bits` in two's complement form (matches Int/Long semantics)
    val maskBits = (1.toBigInt() shl bits) - 1.toBigInt()
    val payload = value and maskBits

    // Align the payload to the top of the K-byte window by shifting left by (8*K - totalBits)
    val leftShift = kBytes * 8 - totalBits
    val w = if (leftShift > 0) (payload shl leftShift) else payload

    // Fast path: byte-aligned start and length is multiple of 8 bits
    if (bitOffset == 0 && (bits and 7) == 0) {
        val byteLen = bits ushr 3
        // For this case, K == byteLen and leftShift == 8*byteLen - bits
        // Stream consecutive bytes of `w` in big-endian order
        var i = 0
        var shift = (byteLen - 1) * 8
        val mask = 0xFF.toBigInt()
        while (i < byteLen) {
            val b = ((w shr shift) and mask).toInt() and 0xFF
            dest[destOffset + i] = b.toByte()
            i++
            shift -= 8
        }
        return
    }

    // Unaligned or non-multiple-of-8 case: build K bytes of the aligned window.
    val mask = 0xFF.toBigInt()
    val firstKeepMask = ((0xFF shl (8 - bitOffset)) and 0xFF)
    val totalTailBits = totalBits and 7 // bits to write in the last byte (0 means full byte)

    // Extract bytes from `w` in big-endian order and merge into destination.
    var i = 0
    var shift = (kBytes - 1) * 8
    while (i < kBytes) {
        val b = ((w shr shift) and mask).toInt() and 0xFF
        val destIndex = destOffset + i
        val writeByte: Int = when (i) {
            0 -> {
                // Merge with preserved high bits of the first destination byte
                val keep = dest[destIndex].toInt() and firstKeepMask
                val putMask = firstKeepMask.inv() and 0xFF
                (keep or (b and putMask)) and 0xFF
            }

            kBytes - 1 -> {
                if (totalTailBits == 0) {
                    // Full overwrite for the last byte
                    b
                } else {
                    // Preserve low (8 - totalTailBits) bits
                    val tailKeepMask = 0xFF ushr totalTailBits
                    val putMask = tailKeepMask.inv() and 0xFF
                    val keep = dest[destIndex].toInt() and tailKeepMask
                    (keep or (b and putMask)) and 0xFF
                }
            }

            else -> b
        }
        dest[destIndex] = writeByte.toByte()
        i++
        shift -= 8
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

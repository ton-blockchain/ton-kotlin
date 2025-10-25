package org.ton.sdk.bitstring.internal

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
            while (len >= 8 && bits <= 56) {
                acc = acc shl 8
                acc = acc or (src[ptr++].toInt() and 0xFF)
                bits += 8
                len -= 8
            }

            while (bits >= 4) {
                bits -= 4
                sb.append(HEX_DIGITS[(acc shr bits) and 15])
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
    src: CharSequence
): Int {
    var hexDigitsCount = 0
    var cmpl = false
    var dstIndex = dstOffset ushr 3
    var bits = 0

    var i = 0
    while (i < src.length) {
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

/** Writes a 32-bit integer to dest in big-endian (network) byte order. */
@Suppress("NOTHING_TO_INLINE")
private inline fun writeIntBE(dest: ByteArray, destOffset: Int, v: Int) {
    dest[destOffset + 0] = (v ushr 24).toByte()
    dest[destOffset + 1] = (v ushr 16).toByte()
    dest[destOffset + 2] = (v ushr 8).toByte()
    dest[destOffset + 3] = (v ushr 0).toByte()
}

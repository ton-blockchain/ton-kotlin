package org.ton.sdk.bitstring.internal

import kotlin.math.min

internal fun bitsCopy(dest: ByteArray, toIndex: Int, src: ByteArray, fromIndex: Int, bitCount: Int) {
    if (bitCount <= 0) return

    fun b(x: Byte): Int = x.toInt() and 0xFF
    fun maskTop(n: Int): Int = if (n <= 0) 0 else (0xFF shl (8 - n)) and 0xFF // highest n bits set
    fun readIntBE(a: ByteArray, off: Int): Int =
        (b(a[off]) shl 24) or (b(a[off + 1]) shl 16) or (b(a[off + 2]) shl 8) or b(a[off + 3])

    var fromByte = fromIndex ushr 3
    var toByte = toIndex ushr 3
    val fromOff = fromIndex and 7
    val toOff = toIndex and 7

    val sz = bitCount
    var sum = bitCount + fromOff // equals C++ bit_count after "+= from_offs"

    if (fromOff == toOff) {
        if (sum < 8) {
            val mask = (maskTop(sum) and (0xFF ushr toOff)) and 0xFF
            val d = b(dest[toByte])
            val s = b(src[fromByte])
            dest[toByte] = ((d and (mask xor 0xFF)) or (s and mask)).toByte()
            return
        }
        val l = sum ushr 3 // full bytes spanned (including first partial)
        if (toOff == 0) {
            // full-byte copy
            // ByteArray.copyInto is multiplatform
            src.copyInto(destination = dest, destinationOffset = toByte, startIndex = fromByte, endIndex = fromByte + l)
        } else {
            val mask = 0xFF ushr toOff
            run {
                val d0 = b(dest[toByte])
                val s0 = b(src[fromByte])
                dest[toByte] = ((d0 and (mask xor 0xFF)) or (s0 and mask)).toByte()
            }
            if (l - 1 > 0) {
                src.copyInto(
                    destination = dest,
                    destinationOffset = toByte + 1,
                    startIndex = fromByte + 1,
                    endIndex = fromByte + l
                )
            }
        }
        sum = sum and 7
        if (sum != 0) {
            val mask = maskTop(sum)
            toByte + (sum ushr 3) + ((sum and 7).let { if (it == 0) 0 else 1 }) // matches to[l]
            val idx = toByte + (((bitCount + fromOff) ushr 3))
            val d = b(dest[idx])
            val s = b(src[idx])
            dest[idx] = ((d and (mask xor 0xFF)) or (s and mask)).toByte()
        }
        return
    }

    var bAccum = toOff
    var acc = if (bAccum != 0) (b(dest[toByte]) ushr (8 - bAccum)).toLong() else 0L

    if (sum < 8) {
        acc = (acc shl sz) or (((b(src[fromByte]) and (0xFF ushr fromOff)) ushr (8 - sum)).toLong())
        bAccum += sz
    } else {
        val lead = 8 - fromOff
        acc = (acc shl lead) or ((b(src[fromByte]) and (0xFF ushr fromOff)).toLong())
        bAccum += lead
        fromByte += 1
        var remain = sum - 8

        while (remain >= 32) {
            acc = (acc shl 32) or (readIntBE(src, fromByte).toLong() and 0xFFFF_FFFFL)
            fromByte += 4
            val out = ((acc ushr bAccum) and 0xFFFF_FFFFL).toInt()
            writeIntBE(dest, toByte, out)
            toByte += 4
            remain -= 32
        }

        while (remain >= 8) {
            acc = (acc shl 8) or b(src[fromByte]).toLong()
            fromByte += 1
            remain -= 8
            bAccum += 8
        }

        if (remain > 0) {
            acc = (acc shl remain) or ((b(src[fromByte]) ushr (8 - remain)).toLong())
            bAccum += remain
        }
    }

    while (bAccum >= 8) {
        bAccum -= 8
        dest[toByte++] = ((acc ushr bAccum) and 0xFF).toByte()
    }
    if (bAccum > 0) {
        val keep = 0xFF ushr bAccum
        val d = (b(dest[toByte]) and keep)
        val newHigh = (((acc and ((1L shl bAccum) - 1)) shl (8 - bAccum)).toInt() and 0xFF)
        dest[toByte] = (d or newHigh).toByte()
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
        val mask = if (bits == 32) -1 else (-1 shl (32 - bits))
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

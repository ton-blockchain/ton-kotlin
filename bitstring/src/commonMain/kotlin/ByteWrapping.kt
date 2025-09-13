package org.ton.kotlin.bitstring

import kotlin.math.min

open class ByteWrappingBitString(
    protected val data: ByteArray,
) : BitString {
    init {
        require(data.isNotEmpty()) { "Data must not be empty" }
        require(data.last().toInt() != 0) { "Bitstring must contain a completion tag bit" }
    }

    private var hashCode: Int = 0

    override val size: Int = data.size * 8 - data.last().countTrailingZeroBits() - 1

    override fun get(index: Int): Boolean {
        if (index !in 0..<size) throw IndexOutOfBoundsException(
            "index ($index) is out of bit string bounds: [0..$size)"
        )
        val byteIndex = index ushr 3
        val bitIndex = 7 - (index and 7)
        val byte = data[byteIndex].toInt()
        val shifted = byte ushr bitIndex
        return shifted and 1 != 0
    }

    override fun getLong(offset: Int, bits: Int): Long {
        if (bits <= 0) return 0

        val r = offset % 8
        val q = offset / 8

        val firstByte = data[q].toLong() and (0xFFL ushr r)
        val rightShift = (8 - (bits + r) % 8) % 8
        if (r + bits <= 8) {
            return firstByte ushr rightShift
        }
        val b = bits - 8 + r
        var result = 0L
        val byteCount = (b + 7) shr 3
        for (i in 1..byteCount) {
            val currentByte = data[q + i].toLong()
            result = (result shl 8) or (currentByte and 0xFF)
        }
        result = result ushr rightShift
        result = result or (firstByte shl b)
        return result
    }

    override fun getByte(offset: Int, bits: Int): Byte {
        if (bits <= 0) return 0

        val r = offset and 7
        val q = offset ushr 3
        val byte = (data[q].toInt() and 0xFF)
        if (r == 0) {
            // xxx_____ -> _____xxx
            //^r
            return (byte ushr (8 - bits)).toByte()
        } else {
            val remainingBits = 8 - r
            if (bits <= remainingBits) {
                // __xxx___ -> _____xxx
                // r^
                return (byte ushr (remainingBits - bits) and ((1 shl bits) - 1)).toByte()
            } else {
                // ______xx|y_______ -> _____xxy
                //     r^
                var result = byte shl 8
                val nextByte = data[q + 1].toInt() and 0xFF
                result = result or nextByte
                return ((result ushr remainingBits) ushr (8 - bits)).toByte()
            }
        }
    }

    override fun toByteArray(): ByteArray {
        return data.copyOf()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toHexString(): String {
        if (size == 0) return ""
        val result = StringBuilder(data.toHexString(HexFormat.UpperCase))
        val lastIndex = result.lastIndex
        when (size % 8) {
            0 -> result.deleteRange(lastIndex - 1, result.length)
            1, 2, 3 -> result[lastIndex] = '_'
            4 -> result.deleteAt(lastIndex)
            else -> result.append('_')
        }
        return result.toString()
    }

    override fun compareTo(other: BitString): Int {
        if (other === this) return 0
        for (i in 0 until min(size, other.size)) {
            val cmp = get(i).compareTo(other[i])
            if (cmp != 0) return cmp
        }
        return size.compareTo(other.size)
    }

    override fun toString(): String = "x{${toHexString()}}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ByteWrappingBitString
        if (other.data.size != data.size) return false
        if (other.hashCode != 0 && hashCode != 0 && other.hashCode != hashCode) return false
        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var hc = hashCode
        if (hc == 0) {
            hc = data.contentHashCode()
            hashCode = hc
        }
        return hc
    }
}

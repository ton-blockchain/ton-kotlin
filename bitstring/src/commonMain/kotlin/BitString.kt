package org.ton.kotlin.bitstring

import kotlinx.io.bytestring.ByteString

interface BitString : Comparable<BitString> {
    val size: Int

    operator fun get(index: Int): Boolean

    fun getLong(offset: Int, bits: Int = Long.SIZE_BITS): Long

    fun getInt(offset: Int, bits: Int = Int.SIZE_BITS): Int = getLong(offset, bits).toInt()

    fun getByte(offset: Int, bits: Int = Byte.SIZE_BITS): Byte = getLong(offset, bits).toByte()

    fun toByteArray(): ByteArray

    fun toHexString(): String

    fun toBinaryString(): String {
        val result = StringBuilder()
        for (i in 0 until size) {
            result.append(if (get(i)) '1' else '0')
        }
        return result.toString()
    }

    companion object {
        val EMPTY = ByteWrappingBitString(byteArrayOf(0x80.toByte()))
        val ALL_ZERO = ByteWrappingBitString(ByteArray(128).apply {
            this[lastIndex] = 0b0000_0001
        })
        val ALL_ONE = ByteWrappingBitString(ByteArray(128) {
            0xFF.toByte()
        })

        fun parseHex(source: CharSequence): BitString {
            val bytes = ByteArray(128)

            var byteIndex = 0
            var hexDigitsCount = 0
            var completionTag = false
            for (c in source) {
                when (c) {
                    in '0'..'9',
                    in 'a'..'f',
                    in 'A'..'F' -> {
                        val value = if (c <= '9') c - '0' else (c.code or 0x20) - 'a'.code + 10
                        if (hexDigitsCount and 1 == 0) {
                            bytes[byteIndex] = (value shl 4).toByte()
                        } else {
                            bytes[byteIndex] = (bytes[byteIndex].toInt() or value).toByte()
                            byteIndex++
                        }
                        hexDigitsCount++
                        continue
                    }

                    '_' -> {
                        completionTag = true
                        break
                    }
                }
            }

            var bits = 4 * hexDigitsCount
            if (completionTag && bits > 0) {
                var t = if (hexDigitsCount and 1 != 0) {
                    (0x100 + bytes[byteIndex]) ushr 4
                } else {
                    0x100 + bytes[--byteIndex]
                }
                while (bits > 0) {
                    if (t == 1) {
                        t = 0x100 + bytes[--byteIndex]
                    }
                    bits--
                    if (t and 1 != 0) {
                        break
                    }
                    t = t ushr 1
                }
            }
            if (bits % 8 == 0) {
                bytes[byteIndex] = 0x80.toByte()
                byteIndex++
            } else if (completionTag) {
                byteIndex++
            }
            val resultBytes = bytes.copyOf(byteIndex)
            return ByteWrappingBitString(resultBytes)
        }
    }
}

val ByteString.indices: IntRange
    get() = 0 until size

interface MutableBitString : BitString {

    fun setBitOne(index: Int)

    operator fun set(index: Int, value: Boolean) {
        if (value) {
            setBitOne(index)
        }
    }

    fun setInt(offset: Int, value: Int, bits: Int = Int.SIZE_BITS) {
        setULong(offset, if (bits == 0) 0uL else (value.toULong() shl (64 - bits)), bits)
    }

    fun setUInt(offset: Int, value: UInt, bits: Int = UInt.SIZE_BITS) {
        setULong(offset, value.toULong(), bits)
    }

    fun setLong(offset: Int, value: Long, bits: Int = Long.SIZE_BITS) {
        setULong(offset, if (bits == 0) 0uL else (value.toULong() shl (64 - bits)), bits)
    }

    fun setULong(offset: Int, value: ULong, bits: Int = ULong.SIZE_BITS)

    fun setBitString(offset: Int, value: BitString, bits: Int) {
        for (i in 0 until bits) {
            set(offset + i, value[i])
        }
    }
}

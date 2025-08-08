package org.ton.kotlin.dht.bucket

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import kotlin.experimental.xor
import kotlin.random.Random

data class Distance(
    val value: ByteString
) : Comparable<Distance> {
    constructor(key1: ByteString, key2: ByteString) : this(ByteString(*ByteArray(32) { i ->
        key1[i] xor key2[i]
    }))

    fun ilog2(): Int? {
        val a = (256 - value.countLeadingZeroBits())
        return if (a > 0) a - 1 else null
    }

    fun bit(index: Int): Boolean {
        require(index in 0..255) { "Index must be in range [0, 255]" }
        return (value[index ushr 3].toInt() shr (7 - (index and 7)) and 1) != 0
    }

    override fun compareTo(other: Distance): Int {
        return value.compareTo(other.value)
    }

    override fun toString(): String = "Distance(${ilog2()}, ${value.toHexString()})"

    private fun ByteString.countLeadingZeroBits(): Int {
        for (i in 0 until 32) {
            val byte = this[i].toInt() and 0xFF
            if (byte != 0) {
                return i * 8 + byte.countLeadingZeroBits8()
            }
        }
        return size * 8
    }

    private fun Int.countLeadingZeroBits8(): Int {
        return when {
            this and 0b1000_0000 != 0 -> 0
            this and 0b0100_0000 != 0 -> 1
            this and 0b0010_0000 != 0 -> 2
            this and 0b0001_0000 != 0 -> 3
            this and 0b0000_1000 != 0 -> 4
            this and 0b0000_0100 != 0 -> 5
            this and 0b0000_0010 != 0 -> 6
            this and 0b0000_0001 != 0 -> 7
            else -> 8
        }
    }

    companion object {
        fun randomDistanceForBucket(i: Int, random: Random = Random): Distance {
            val bytes = ByteArray(32)

            val quot = i / 8
            val rem = i % 8

            val from = 32 - quot
            if (from < 32) {
                random.nextBytes(bytes, from, 32)
            }

            val pivot = 31 - quot
            val lower = 1 shl rem
            val upper = 1 shl (rem + 1)
            bytes[pivot] = random.nextInt(lower, upper).toByte()

            return Distance(ByteString(*bytes))
        }
    }
}

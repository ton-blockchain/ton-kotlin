package org.ton.sdk.bitstring

import org.ton.sdk.bitstring.internal.bitsCompare
import org.ton.sdk.bitstring.internal.bitsCopy
import org.ton.sdk.bitstring.internal.bitsParseHex
import org.ton.sdk.bitstring.internal.bitsToHex
import kotlin.jvm.JvmField
import kotlin.math.min

private val KEEP_MASK = intArrayOf(0x00, 0x80, 0xC0, 0xE0, 0xF0, 0xF8, 0xFC, 0xFE)
private val TAG_MASK = intArrayOf(0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01)

public class BitString internal constructor(
    private var data: ByteArray,
    size: Int,
    private var hashCode: Int = 0
) : Comparable<BitString> {
    public var size: Int = size
        private set

    public constructor(string: String) : this(byteArrayOf(), 0) {
        if (string.startsWith("x{") && string.endsWith("}")) {
            val data = ByteArray((string.length - 2) ushr 1)
            size = bitsParseHex(data, 0, string, 2, string.length - 1)
            this.data = data
        } else {
            val data = ByteArray((string.length + 1) ushr 1)
            size = bitsParseHex(data, 0, string, 0, string.length)
            this.data = data
        }
    }

    public constructor(data: ByteArray) : this(
        data = data,
        size = if (data.isEmpty()) 0 else data.size * 8 - data.last().countTrailingZeroBits() - 1
    )

    public constructor(data: ByteArray, size: Int) : this(data.copyOf(), size, 0)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BitString) return false
        if (other.size != size) return false
        if (other.hashCode != 0 && hashCode != 0 && other.hashCode != hashCode) return false
        return bitsCompare(data, other.data, size) == 0
    }

    override fun hashCode(): Int {
        var hc = hashCode
        if (hc == 0) {
            val length = (size + 7) ushr 3
            var result = 1
            for (i in 0 until length - 1) {
                result = 31 * result + data[i]
            }
            // Handle last byte specially to consider only valid bits
            if (length > 0) {
                val lastByte = data[length - 1]
                val validBits = size and 7
                val mask = if (validBits == 0) 0xFF else (0xFF shl (8 - validBits))
                result = 31 * result + (lastByte.toInt() and mask)
            }
            hc = result
            hashCode = hc
        }
        return hc
    }

    public operator fun get(index: Int): Boolean {
        if (index !in 0..<size) throw IndexOutOfBoundsException(
            "index ($index) is out of bit string bounds: [0..$size)"
        )
        val byteIndex = index ushr 3
        val bitIndex = index and 7
        return ((data[byteIndex].toInt() ushr (7 - bitIndex)) and 0x01) != 0
    }

    public fun toByteArray(): ByteArray {
        val result = data.copyOf((size + 8) ushr 3)
        val i = size ushr 3
        val r = size and 7

        val cur = result[i].toInt() and 0xFF
        result[i] = ((cur and KEEP_MASK[r]) or TAG_MASK[r]).toByte()
        return result
    }

    public fun substring(startIndex: Int, endIndex: Int = size): BitString = if (startIndex == endIndex) {
        EMPTY
    } else {
        require(startIndex in 0..size) { "startIndex ($startIndex) is out of bit string bounds: [0..$size]" }
        require(endIndex in 0..size) { "endIndex ($endIndex) is out of bit string bounds: [0..$size]" }
        require(startIndex <= endIndex) { "startIndex ($startIndex) is greater than endIndex ($endIndex)" }
        val bitsLength = endIndex - startIndex
        val bytesLength = (bitsLength + 7) ushr 3
        val resultBytes = ByteArray(bytesLength)
        bitsCopy(resultBytes, 0, 0, data, startIndex, bitsLength)
        BitString(resultBytes, bitsLength, 0)
    }

    override fun compareTo(other: BitString): Int {
        if (other === this) return 0
        val compare = bitsCompare(data, other.data, min(size, other.size))
        return if (compare != 0) {
            compare
        } else {
            size.compareTo(other.size)
        }
    }

    override fun toString(): String {
        val sb = StringBuilder(((size + 7) ushr 2) + 3)
        sb.append("x{")
        bitsToHex(sb, data, 0, 0, size)
        sb.append("}")
        return sb.toString()
    }

    /**
     * Returns a reference to the underlying array.
     *
     * These methods return reference to the underlying array, not to its copy.
     * Consider using [toByteArray] if it's impossible to guarantee that the array won't be modified.
     */
    @PublishedApi
    internal fun getBackingArrayReference(): ByteArray = data

    public companion object {
        @JvmField
        public val EMPTY: BitString = BitString(ByteArray(0), 0, 0)
    }
}

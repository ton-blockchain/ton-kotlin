package org.ton.bitstring

import org.ton.bitstring.exception.BitStringUnderflowException
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor
import kotlin.jvm.JvmStatic
import kotlin.math.min

public open class ByteBackedBitString protected constructor(
    override val size: Int,
    public open val bytes: ByteArray
) : BitString {
    private val hashCode by lazy(LazyThreadSafetyMode.PUBLICATION) {
        var result = size
        result = 31 * result + bytes.contentHashCode()
        result
    }

    override operator fun get(index: Int): Boolean = getOrNull(index) ?: throw BitStringUnderflowException()

    override fun getOrNull(index: Int): Boolean? =
        if (index in 0..size) get(bytes, index) else null

    override fun countLeadingBits(fromIndex: Int, toIndex: Int, bit: Boolean): Int {
        return countLeadingBits(bytes, fromIndex, toIndex - fromIndex, bit)
    }

    override fun plus(bits: BitString): BitString = toMutableBitString().plus(bits)
    override fun plus(bytes: ByteArray): BitString = toMutableBitString().plus(bytes)
    override fun plus(bytes: ByteArray, bits: Int): BitString = toMutableBitString().plus(bytes, bits)

    // TODO: fix
//    override fun slice(startIndex: Int, endIndex: Int): BitString = slice(startIndex..endIndex)
//
//    override fun slice(indices: IntRange): BitString {
//        val result = ByteBackedMutableBitString.of(size = indices.last - indices.first)
//        for ((position, i) in indices.withIndex()) {
//            result[position] = get(i)
//        }
//        return result
//    }

    override fun toByteArray(augment: Boolean): ByteArray =
        if (augment && (size % 8 != 0)) {
            appendAugmentTag(bytes, size)
        } else {
            bytes.copyOf((size + 7) ushr 3)
        }

    override fun toBooleanArray(): BooleanArray = toList().toBooleanArray()

    override fun toMutableBitString(): MutableBitString =
        ByteBackedMutableBitString.of(bytes.copyOf((size + 7) ushr 3), size)

    override fun toBitString(): BitString = ByteBackedBitString(size, bytes.copyOf((size + 7) ushr 3))

    override fun iterator(): Iterator<Boolean> = BitStringIterator(this)

    override fun xor(other: BitString): BitString {
        return if (other !is ByteBackedBitString) {
            val result = ByteBackedMutableBitString.of(maxOf(size, other.size))
            for (i in 0 until min(size, other.size)) {
                result[i] = get(i) xor other[i]
            }
            result
        } else {
            val result = ByteArray(maxOf(bytes.size, other.bytes.size))
            for (i in 0 until min(bytes.size, other.bytes.size)) {
                result[i] = bytes[i] xor other.bytes[i]
            }
            of(result, maxOf(size, other.size))
        }
    }

    override fun or(other: BitString): BitString {
        return if (other !is ByteBackedBitString) {
            val result = ByteBackedMutableBitString.of(maxOf(size, other.size))
            for (i in 0 until min(size, other.size)) {
                result[i] = get(i) or other[i]
            }
            result
        } else {
            val result = ByteArray(maxOf(bytes.size, other.bytes.size))
            for (i in 0 until min(bytes.size, other.bytes.size)) {
                result[i] = bytes[i] or other.bytes[i]
            }
            of(result, maxOf(size, other.size))
        }
    }

    override fun copyInto(destination: MutableBitString, destinationOffset: Int, startIndex: Int, endIndex: Int) {
        if (destination !is ByteBackedBitString) {
            return super.copyInto(destination, destinationOffset, startIndex, endIndex)
        }
        bitsCopy(destination.bytes, destinationOffset, bytes, startIndex, endIndex - startIndex)
    }

    override fun toString(): String = "x{${toHexString()}}"

    override fun toHexString(): String {
        if (size == 0) return ""
        val data = appendTag(bytes, size)
        val result = StringBuilder(data.toHexString())
        when (size % 8) {
            0 -> {
                result.deleteAt(result.lastIndex)
                result.deleteAt(result.lastIndex)
            }

            in 1..3 -> {
                result[result.lastIndex] = '_'
            }

            4 -> result.deleteAt(result.lastIndex)
            else -> result.append('_')
        }
        return result.toString().uppercase()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BitString) return false
        if (size != other.size) return false
        if (other is ByteBackedBitString) {
            if (!bytes.contentEquals(other.bytes)) return false
        } else {
            if (!toBooleanArray().contentEquals(other.toBooleanArray())) return false
        }
        return true
    }

    override fun hashCode(): Int = hashCode

    internal open class BitStringIterator(
        val bitString: BitString,
        var index: Int = 0
    ) : ListIterator<Boolean> {
        override fun hasNext(): Boolean = index < bitString.size
        override fun hasPrevious(): Boolean = index - 1 >= 0
        override fun next(): Boolean = bitString[index++]
        override fun nextIndex(): Int = index + 1
        override fun previous(): Boolean = bitString[index--]
        override fun previousIndex(): Int = index - 1
    }

    private fun appendTag(data: ByteArray, bits: Int): ByteArray {
        val shift = bits % 8
        if (shift == 0 || data.isEmpty()) {
            val newData = data.copyOf(bits / 8 + 1)
            newData[newData.lastIndex] = 0x80.toByte()
            return newData
        } else {
            val newData = data.copyOf(bits / 8 + 1)
            var lastByte = newData[newData.lastIndex].toInt()
            if (shift != 7) {
                lastByte = lastByte shr (7 - shift)
            }
            lastByte = lastByte or 1
            if (shift != 7) {
                lastByte = lastByte shl (7 - shift)
            }
            newData[newData.lastIndex] = lastByte.toByte()
            return newData
        }
    }

    public companion object {
        @JvmStatic
        public fun of(size: Int = 0): ByteBackedBitString = ByteBackedBitString(size, constructByteArray(size))

        @JvmStatic
        public fun of(
            bytes: ByteArray = ByteArray(0),
            size: Int = bytes.size * Byte.SIZE_BITS
        ): ByteBackedBitString = ByteBackedBitString(size, constructByteArray(bytes, size))

        @JvmStatic
        protected fun constructByteArray(bytes: ByteArray, size: Int): ByteArray {
            return bytes.copyOf(bytesSize(size))
        }

        @JvmStatic
        protected fun expandByteArray(bytes: ByteArray, size: Int): ByteArray {
            val requiredBytesSize = bytesSize(size)
            return when {
                bytes.size < requiredBytesSize -> constructByteArray(bytes, size)
                else -> bytes
            }
        }

        @JvmStatic
        protected fun constructByteArray(size: Int): ByteArray {
            return ByteArray(bytesSize(size))
        }

        @JvmStatic
        protected fun get(bytes: ByteArray, index: Int): Boolean {
            val byteIndex = index / Byte.SIZE_BITS
            val bitMask = (1 shl (7 - (index % Byte.SIZE_BITS))).toByte()
            return (bytes[byteIndex] and bitMask) != 0.toByte()
        }

        private fun bytesSize(bits: Int): Int {
            return bits / Byte.SIZE_BITS + if (bits % Byte.SIZE_BITS == 0) 0 else 1
        }
    }
}

internal inline val Int.byteIndex get() = this / Byte.SIZE_BITS
internal inline val Int.bitMask get() = (1 shl (7 - (this % Byte.SIZE_BITS))).toByte()

private fun appendAugmentTag(data: ByteArray, bits: Int): ByteArray {
    val shift = bits % Byte.SIZE_BITS
    if (shift == 0 || data.isEmpty()) {
        val newData = data.copyOf(bits / Byte.SIZE_BITS + 1)
        newData[newData.lastIndex] = 0x80.toByte()
        return newData
    } else {
        val newData = data.copyOf(bits / Byte.SIZE_BITS + 1)
        var lastByte = newData[newData.lastIndex].toInt() and 0xFF
        if (shift != 7) {
            lastByte = lastByte shr (7 - shift)
        }
        lastByte = lastByte or 1
        if (shift != 7) {
            lastByte = lastByte shl (7 - shift)
        }
        newData[newData.lastIndex] = lastByte.toByte()
        return newData
    }
}

internal fun bitsCopy(dest: ByteArray, toIndex: Int, src: ByteArray, fromIndex: Int, bitCount: Int) {
    if (bitCount <= 0) return

    var srcOffset = fromIndex shr 3
    var destOffset = toIndex shr 3
    val fromOffset = fromIndex and 7
    val toOffset = toIndex and 7

    var remainingBits = bitCount
    val bitCountTotal = bitCount + fromOffset

    if (fromOffset == toOffset) {
        if (bitCountTotal < 8) {
            val mask = ((-0x100 ushr bitCountTotal) and (0xff ushr toOffset))
            dest[destOffset] = ((dest[destOffset].toInt() and mask.inv()) or (src[srcOffset].toInt() and mask)).toByte()
            return
        }

        val bytesToCopy = bitCountTotal shr 3
        if (toOffset == 0) {
            src.copyInto(dest, destOffset, srcOffset, srcOffset + bytesToCopy)
        } else {
            val mask = (0xff ushr toOffset)
            dest[destOffset] = ((dest[destOffset].toInt() and mask.inv()) or (src[srcOffset].toInt() and mask)).toByte()
            src.copyInto(dest, destOffset + 1, srcOffset + 1, srcOffset + bytesToCopy)
        }

        if (bitCountTotal and 7 != 0) {
            val mask = (-0x100 ushr (bitCountTotal and 7))
            dest[destOffset + bytesToCopy] =
                ((dest[destOffset + bytesToCopy].toInt() and mask.inv()) or (src[srcOffset + bytesToCopy].toInt() and mask)).toByte()
        }
    } else {
        var bitsInAcc = toOffset
        var accumulator = if (bitsInAcc != 0) dest[destOffset].toLong() ushr (8 - bitsInAcc) else 0L

        if (bitCountTotal < 8) {
            accumulator = accumulator shl remainingBits
            accumulator =
                accumulator or ((src[srcOffset].toLong() and (0xffL ushr fromOffset)) ushr (8 - bitCountTotal))
            bitsInAcc += remainingBits
        } else {
            val leadingBits = 8 - fromOffset
            accumulator = (accumulator shl leadingBits)
            accumulator = accumulator or (src[srcOffset++].toLong() and (0xffL ushr fromOffset))
            bitsInAcc += leadingBits
            remainingBits -= leadingBits

            while (remainingBits >= 8) {
                accumulator = accumulator shl 8
                accumulator = accumulator or (src[srcOffset++].toLong() and 0xffL)
                dest[destOffset++] = (accumulator ushr bitsInAcc).toByte()
                remainingBits -= 8
            }

            if (remainingBits > 0) {
                accumulator =
                    (accumulator shl remainingBits) or ((src[srcOffset].toLong() and 0xff) ushr (8 - remainingBits))
                bitsInAcc += remainingBits
            }
        }

        while (bitsInAcc >= 8) {
            bitsInAcc -= 8
            dest[destOffset++] = (accumulator ushr bitsInAcc).toByte()
        }

        if (bitsInAcc > 0) {
            dest[destOffset] =
                ((dest[destOffset].toInt() and (0xff ushr bitsInAcc)) or (accumulator.toInt() shl (8 - bitsInAcc))).toByte()
        }
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
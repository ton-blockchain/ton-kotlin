package org.ton.sdk.bitstring

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.unsafe.UnsafeByteStringApi
import kotlinx.io.bytestring.unsafe.UnsafeByteStringOperations
import org.ton.sdk.bigint.BigInt
import org.ton.sdk.bigint.toBigInt
import org.ton.sdk.bitstring.internal.*

public class BitStringBuilder : BitSink {
    @Suppress("OPT_IN_OVERRIDE")
    override val buffer: ByteArray = ByteArray(128)
    public var bitLength: Int = 0
        private set

    override fun transferFrom(source: BitSource): Int {
        val bitCount = source.transferTo(this)
        bitLength += bitCount
        return bitCount
    }

    override fun write(source: BitSource, bitCount: Int) {
        val transferredBitCount = source.readAtMostTo(this, bitCount)
        bitLength += transferredBitCount
    }

    override fun write(source: BitString, startIndex: Int, endIndex: Int) {
        val bitCount = endIndex - startIndex
        bitsCopy(
            buffer,
            bitLength,
            source.getBackingArrayReference(),
            startIndex,
            bitCount
        )
        bitLength += bitCount
    }

    override fun write(source: ByteArray, startIndex: Int, endIndex: Int) {
        val bitCount = (endIndex - startIndex) * 8
        bitsCopy(
            buffer,
            bitLength,
            source,
            startIndex * 8,
            bitCount
        )
        bitLength += bitCount
    }

    @OptIn(UnsafeByteStringApi::class)
    override fun write(source: ByteString, startIndex: Int, endIndex: Int) {
        val bitCount = (endIndex - startIndex) * 8
        UnsafeByteStringOperations.withByteArrayUnsafe(
            source
        ) {
            bitsCopy(
                buffer,
                bitLength,
                it,
                startIndex * 8,
                bitCount
            )
        }
        bitLength += bitCount
    }

    private fun storeBitOne() {
        val q = bitLength / 8
        val r = bitLength % 8
        buffer[q] = (buffer[q].toInt() or (1 shl (7 - r))).toByte()
        bitLength++
    }

    private fun storeBitZero() {
        bitLength++
    }

    override fun writeBit(bit: Boolean) {
        if (bit) {
            storeBitOne()
        } else {
            storeBitZero()
        }
    }

    override fun writeByte(byte: Byte) {
        val q = bitLength / 8
        val r = bitLength % 8
        if (r == 0) {
            // xxxxxxxx
            buffer[q] = byte
        } else {
            // yyyxxxxx|xxx00000
            val uByte = (byte.toInt() and 0xFF)
            buffer[q] = ((buffer[q].toInt() and 0xFF) or (uByte ushr r)).toByte()
            buffer[q + 1] = (uByte shl (8 - r)).toByte()
        }
        bitLength += 8
    }

    override fun writeInt(int: Int) {
        storeIntIntoByteArray(buffer, 0, bitLength, int, Int.SIZE_BITS)
        bitLength += Int.SIZE_BITS
    }

    override fun writeShort(short: Short) {
        storeIntIntoByteArray(buffer, 0, bitLength, short.toInt(), Short.SIZE_BITS)
        bitLength += Short.SIZE_BITS
    }

    override fun writeLong(long: Long) {
        storeLongIntoByteArray(buffer, 0, bitLength, long, Long.SIZE_BITS)
        bitLength += Long.SIZE_BITS
    }

    override fun writeInt(value: Int, bitCount: Int) {
        val shifted = (value shl (32 - bitCount)) shr (32 - bitCount)
        require(shifted == value) { "Value $value does not fit into $bitCount-bit signed int" }
        writeUIntUnchecked(value, bitCount)
    }

    override fun writeUInt(value: Int, bitCount: Int) {
        val shifted = (value shl (32 - bitCount)) ushr (32 - bitCount)
        require(shifted == value) { "Value $value does not fit into $bitCount unsigned bits" }
        writeUIntUnchecked(value, bitCount)
    }

    override fun writeLong(value: Long, bitCount: Int) {
        val shifted = (value shl (64 - bitCount)) shr (64 - bitCount)
        require(shifted == value) { "Value $value does not fit into $bitCount unsigned bits" }
        writeULongUnchecked(value, bitCount)
    }

    override fun writeULong(value: Long, bitCount: Int) {
        val shifted = (value shl (64 - bitCount)) ushr (64 - bitCount)
        require(shifted == value) { "Value $value does not fit into $bitCount unsigned bits" }
        writeULongUnchecked(value, bitCount)
    }


    override fun writeUBigInt(value: BigInt, bitCount: Int) {
        storeBigIntIntoByteArray(buffer, 0, bitLength, value, bitCount)
        bitLength += bitCount
    }

    public fun toBitString(): BitString {
        return BitString(buffer, bitLength)
    }

    override fun toString(): String = buildString {
        append("BitStringBuilder(bitLength=")
        append(bitLength)
        append(", buffer=x{")
        bitsToHex(this, buffer, 0, 0, bitLength)
        append("})")
    }

    private fun writeUIntUnchecked(value: Int, bitCount: Int) {
        if (bitCount <= Int.SIZE_BITS) {
            storeIntIntoByteArray(buffer, 0, bitLength, value, bitCount)
        } else if (bitCount <= Long.SIZE_BITS) {
            storeLongIntoByteArray(buffer, 0, bitLength, value.toLong(), bitCount)
        } else {
            storeBigIntIntoByteArray(buffer, 0, bitLength, value.toBigInt(), bitCount)
        }
        bitLength += bitCount
    }

    private fun writeULongUnchecked(value: Long, bitCount: Int) {
        if (bitCount <= Long.SIZE_BITS) {
            storeLongIntoByteArray(buffer, 0, bitLength, value, bitCount)
        } else {
            storeBigIntIntoByteArray(buffer, 0, bitLength, value.toBigInt(), bitCount)
        }
        bitLength += bitCount
    }
}

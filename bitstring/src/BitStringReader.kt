package org.ton.sdk.bitstring

import kotlinx.io.DelicateIoApi
import kotlinx.io.Sink
import kotlinx.io.UnsafeIoApi
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.unsafe.UnsafeByteStringApi
import kotlinx.io.bytestring.unsafe.UnsafeByteStringOperations
import kotlinx.io.unsafe.UnsafeBufferOperations
import kotlinx.io.writeToInternalBuffer
import org.ton.sdk.bigint.*
import org.ton.sdk.bitstring.internal.bitsCopy
import kotlin.math.min

public class BitStringReader(
    private val bitString: BitString
) : BitSource {
    private var position = 0

    override fun readBit(): Boolean {
        return bitString[position++]
    }

    override fun readByte(): Byte {
        return readByte(8)
    }

    override fun readInt(): Int {
        return readBigInt(Short.SIZE_BITS).toInt()
    }

    override fun readShort(): Short {
        return readBigInt(Short.SIZE_BITS).toInt().toShort()
    }

    override fun readLong(): Long {
        return readBigInt(Long.SIZE_BITS).toLong()
    }

    override fun skip(bitCount: Int) {
        position += bitCount
    }

    override fun readAtMostTo(sink: ByteArray, startIndex: Int, endIndex: Int): Int {
        val bitCount = (endIndex - startIndex) * 8
        val remaining = bitString.size - position
        val read = min(bitCount, remaining)
        bitsCopy(
            sink,
            0,
            startIndex * 8,
            bitString.getBackingArrayReference(),
            position,
            read
        )
        position += read
        return read / 8
    }

    override fun readAtMostTo(sink: BitSink, bitCount: Int): Int {
        val remaining = bitString.size - position
        val read = min(bitCount, remaining)
        sink.write(bitString, position, position + read)
        position += read
        return read
    }

    override fun transferTo(sink: BitSink): Int {
        sink.write(bitString, position)
        val bits = bitString.size - position
        position = bitString.size
        return bits
    }

    override fun readInt(bitCount: Int): Int {
        return readBigInt(bitCount).toInt()
    }

    override fun readLong(bitCount: Int): Long {
        return readBigInt(bitCount).toLong()
    }

    override fun readBigInt(bitCount: Int): BigInt {
        if (bitCount == 0) return 0.toBigInt()
        val rem = bitCount % 8
        val buffer = readBitString(bitCount)
        var int = buffer.getBackingArrayReference().toBigInt()
        if (rem != 0) {
            int = int shr (8 - rem)
        }
        return int
    }

    override fun readBitString(bitCount: Int): BitString {
        val result = bitString.substring(position, position + bitCount)
        position += bitCount
        return result
    }

    override fun readByteArray(byteCount: Int): ByteArray {
        val bytes = ByteArray(byteCount)
        bitsCopy(
            bytes,
            0,
            0,
            bitString.getBackingArrayReference(),
            position,
            byteCount * 8
        )
        position += byteCount * 8
        return bytes
    }

    @OptIn(UnsafeByteStringApi::class)
    override fun readByteString(byteCount: Int): ByteString {
        val bytes = ByteArray(byteCount)
        bitsCopy(
            bytes,
            0,
            0,
            bitString.getBackingArrayReference(),
            position,
            byteCount * 8
        )
        position += byteCount * 8
        return UnsafeByteStringOperations.wrapUnsafe(bytes)
    }

    override fun readTo(sink: BitSink, bitCount: Int) {
        sink.write(
            bitString,
            position,
            bitCount
        )
        position += bitCount
    }

    @OptIn(DelicateIoApi::class, UnsafeIoApi::class)
    override fun readTo(sink: Sink, byteCound: Int) {
        var currentOffset = 0
        while (currentOffset < byteCound) {
            sink.writeToInternalBuffer { buffer ->
                UnsafeBufferOperations.writeToTail(buffer, 1) { data, pos, limit ->
                    val toCopy = minOf(byteCound - currentOffset, limit - pos)
                    bitsCopy(
                        data,
                        0,
                        pos * 8,
                        bitString.getBackingArrayReference(),
                        position + currentOffset * 8,
                        toCopy * 8
                    )
                    currentOffset += toCopy
                    toCopy
                }
            }
        }
        position += byteCound * 8
    }

    private fun readByte(bitCount: Int): Byte {
        if (bitCount == 0) return 0
        val index = position
        val r = index % 8
        val q = index / 8
        val byte = bitString.getBackingArrayReference()[q].toInt() and 0xFF
        if (r == 0) {
            return (byte ushr (8 - bitCount)).toByte()
        } else if (bitCount <= (8 - bitCount)) {
            return ((byte ushr (8 - r - bitCount)) and ((1 shl bitCount) - 1)).toByte()
        } else {
            var res = byte shl 8
            val nextByte = bitString.getBackingArrayReference()[q + 1].toInt() and 0xFF
            res = res or nextByte
            return ((res ushr (8 - r)) ushr (8 - bitCount)).toByte()
        }
    }
}

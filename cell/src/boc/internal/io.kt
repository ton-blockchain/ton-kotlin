package org.ton.sdk.cell.boc.internal

import kotlinx.io.*
import org.ton.sdk.bitstring.BitString
import org.ton.sdk.bitstring.unsafe.UnsafeBitStringOperations
import org.ton.sdk.cell.CellDescriptor
import org.ton.sdk.cell.LevelMask
import org.ton.sdk.crypto.HashBytes
import kotlin.math.min

internal fun Source.readLong(bytes: Int): Long {
    var result = 0L
    for (i in 0 until bytes) {
        result = (result shl 8) or (readByte().toLong() and 0xFF)
    }
    return result
}

internal fun Source.readHashes(levelMask: LevelMask): Array<HashBytes> {
    var lastHash = HashBytes(readByteString(32))
    return Array(LevelMask.MAX_LEVEL + 1) { level ->
        if (level !in levelMask || level == 0) {
            lastHash
        } else {
            val hash = HashBytes(readByteString(32))
            lastHash = hash
            hash
        }
    }
}

internal fun Source.readDepths(levelMask: LevelMask): IntArray {
    var lastDepth = readUShort().toInt()
    return IntArray(LevelMask.MAX_LEVEL + 1) { level ->
        if (level !in levelMask || level == 0) {
            lastDepth
        } else {
            val depth = readUShort().toInt()
            lastDepth = depth
            depth
        }
    }
}

internal fun Source.readBits(descriptor: CellDescriptor): BitString {
    val dataLength = descriptor.byteLength
    val data = readByteArray(dataLength)
    val bitLength = if (descriptor.isAligned) {
        data.size * 8
    } else {
        data.size * 8 - data.last().countTrailingZeroBits() - 1
    }
    @Suppress("OPT_IN_USAGE")
    return UnsafeBitStringOperations.wrapUnsafe(data, bitLength)
}

internal fun RawSource.buffer(byteCount: Long): Buffer {
    val buffer = Buffer()
    var remaining = byteCount
    while (remaining > 0) {
        val read = readAtMostTo(buffer, min(remaining, 8192))
        if (read == -1L) throw EOFException("Source doesn't contain required number of bytes ($byteCount).")
        remaining -= read
    }
    return buffer
}

/**
 * A RawSource that supports random access via an absolute byte position.
 *
 * Semantics:
 * - Position is an absolute offset from the beginning in bytes.
 * - Reads start from [position] and advance it by the number of bytes read.
 * - [seek] moves [position] without reading.
 * - Implementations must reject negative positions and closed access.
 * - If [size] is known (>= 0), seeking past [size] is prohibited.
 */
internal interface SeekableRawSource : RawSource {
    /**
     * Current read position in bytes from the beginning.
     *
     * Reading always starts at this position.
     */
    var position: Long

    /**
     * Total size in bytes if known, or -1 if unknown.
     *
     * When non-negative, implementations must guarantee that:
     * - 0 <= position <= size
     * - readAtMostTo(...) returns -1 once position == size and no further bytes are available.
     */
    val size: Long

    /**
     * Sets [position] to the given absolute offset.
     *
     * @throws IllegalArgumentException if [newPosition] < 0.
     * @throws IllegalArgumentException if [size] >= 0 and [newPosition] > [size].
     * @throws IllegalStateException if the source is closed.
     */
    fun seek(newPosition: Long)
}

/**
 * SeekableRawSource over a ByteArray.
 *
 * Guarantees:
 * - size is always known and equals byteArray.size.
 * - Thread-safety is not provided.
 */
internal class ByteArraySeekableRawSource(
    private val byteArray: ByteArray
) : SeekableRawSource {

    private var closed: Boolean = false

    override var position: Long = 0
        set(newPosition) {
            check(!closed) { "Source is closed" }
            require(newPosition >= 0L) { "newPosition < 0: $newPosition" }
            require(newPosition <= size) {
                "newPosition ($newPosition) > size ($size)"
            }
            field = newPosition
        }

    override val size: Long
        get() = byteArray.size.toLong()

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        require(byteCount >= 0L) { "byteCount < 0: $byteCount" }
        check(!closed) { "Source is closed" }

        if (position >= size) {
            return -1L
        }

        if (byteCount == 0L) {
            // Contract allows 0; read nothing and report 0 without signaling EOF.
            return 0L
        }

        val remaining = size - position
        val toRead = min(byteCount, remaining)
        if (toRead <= 0L) {
            return -1L
        }

        // ByteArray.size is Int, and position/toRead are constrained by [size].
        val offset = position.toInt()
        val length = toRead.toInt()

        sink.write(byteArray, offset, offset + length)
        position += toRead
        return toRead
    }

    override fun seek(newPosition: Long) {
        position = newPosition
    }

    override fun close() {
        closed = true
    }
}

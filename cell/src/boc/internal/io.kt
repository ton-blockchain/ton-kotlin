package org.ton.sdk.cell.boc.internal

import kotlinx.io.*
import kotlinx.io.bytestring.ByteString

internal fun Source.readLong(bytes: Int): Long {
    var result = 0L
    for (i in 0 until bytes) {
        result = (result shl 8) or (readByte().toLong() and 0xFF)
    }
    return result
}

internal interface RandomAccess {
    var position: Long
}

internal interface RandomAccessSink : RandomAccess, RawSink, AutoCloseable

internal interface RandomAccessSource : RandomAccess, RawSource, AutoCloseable

internal interface RandomAccessStorage : RandomAccessSink, RandomAccessSource, AutoCloseable

internal class ByteArrayRandomAccessStorage(
    private val byteArray: ByteArray
) : RandomAccessStorage {
    override var position: Long = 0

    override fun write(source: Buffer, byteCount: Long) {
        require(byteCount >= 0) { "byteCount < 0: $byteCount" }
        require(position >= 0) { "position < 0: $position" }
        require(position <= Int.MAX_VALUE) { "position too large: $position" }
        require(byteCount <= Int.MAX_VALUE) { "byteCount too large: $byteCount" }

        val pos = position.toInt()
        val toWrite = byteCount.toInt()
        val end = pos + toWrite
        require(end <= byteArray.size) {
            "Not enough space: position=$pos, byteCount=$toWrite, size=${byteArray.size}"
        }

        var remaining = toWrite
        var offset = pos
        while (remaining > 0) {
            val read = source.readAtMostTo(byteArray, offset, remaining)
            if (read == -1) throw EOFException("Source exhausted before writing $toWrite bytes")
            offset += read
            remaining -= read
        }
        position = end.toLong()
    }

    override fun flush() {
    }

    override fun close() {
    }

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        val bytesToRead = byteCount.coerceAtMost((byteArray.size - position.toInt()).toLong())
        if (bytesToRead <= 0) return 0
        sink.write(byteArray, position.toInt(), (position + bytesToRead).toInt())
        position += bytesToRead
        return bytesToRead
    }
}

internal class ByteStringRandomAccessSource(
    private val byteString: ByteString
) : RandomAccessSource {
    override var position: Long = 0

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        val bytesToRead = byteCount.coerceAtMost((byteString.size - position))
        if (bytesToRead <= 0) return 0
        sink.write(byteString, position.toInt(), (position + bytesToRead).toInt())
        position += bytesToRead
        return bytesToRead
    }

    override fun close() {
    }
}

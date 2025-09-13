package org.ton.kotlin.cell.internal

import io.ktor.utils.io.core.*
import kotlinx.io.*
import kotlinx.io.Buffer
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
        source.readFully(byteArray, position.toInt(), byteCount.toInt())
        position += byteCount
    }

    override fun flush() {
    }

    override fun close() {
    }

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        val bytesToRead = byteCount.coerceAtMost((byteArray.size - position.toInt()).toLong())
        if (bytesToRead <= 0) return 0
        sink.write(byteArray, position.toInt(), bytesToRead.toInt())
        position += bytesToRead
        return bytesToRead
    }
}

public class ByteStringRandomAccessSource(
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

//public expect fun FileSystem.randomAccessSource(path: Path): RandomAccessSource
//
//public expect fun FileSystem.randomAccessStorage(path: Path): RandomAccessStorage

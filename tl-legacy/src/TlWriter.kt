@file:Suppress("DEPRECATION")

package org.ton.tl

import kotlinx.io.*
import kotlinx.io.bytestring.ByteString
import org.ton.tl.constructors.Bool
import org.ton.tl.constructors.BoolTlCombinator

@Deprecated(DEPRECATION_MESSAGE)
public class TlWriter(
    public val output: Sink
) {
    public fun writeBoolean(value: Boolean) {
        BoolTlCombinator.encode(this, Bool[value])
    }

    public fun writeInt(value: Int) {
        output.writeIntLe(value)
    }

    public fun writeLong(value: Long) {
        output.writeLongLe(value)
    }

    public fun writeRaw(value: ByteArray) {
        output.write(value)
    }

    public fun writeRaw(value: ByteString) {
        output.write(value)
    }

    public fun writeBytes(value: ByteString, startIndex: Int = 0, endIndex: Int = value.size) {
        val length = endIndex - startIndex
        val lengthSize = writeLength(length)
        var totalLength = length + lengthSize
        output.write(value, startIndex, endIndex)
        while (totalLength++ % 4 > 0) {
            output.writeByte(0)
        }
    }

    public fun writeBytes(value: ByteArray, startIndex: Int = 0, endIndex: Int = value.size) {
        val length = endIndex - startIndex
        val lengthSize = writeLength(length)
        var totalLength = length + lengthSize
        output.write(value, startIndex, endIndex)
        while (totalLength++ % 4 > 0) {
            output.writeByte(0)
        }
    }

    private fun writeLength(length: Int): Int {
        if (length < 254) {
            output.writeUByte(length.toUByte())
            return 1
        } else if (length < (1 shl 24)) {
            output.writeUByte(254u)
            output.writeUByte((length and 255).toUByte())
            output.writeUByte(((length shr 8) and 255).toUByte())
            output.writeUByte((length shr 16).toUByte())
            return 4
        } else if (length < Int.MAX_VALUE) {
            output.writeUByte(255u)
            output.writeUByte((length and 255).toUByte())
            output.writeUByte(((length shr 8) and 255).toUByte())
            output.writeUByte(((length shr 16) and 255).toUByte())
            output.writeUByte(((length shr 24) and 255).toUByte())
            output.writeByte(0)
            output.writeByte(0)
            output.writeByte(0)
            return 8
        } else {
            throw IllegalStateException("Too big byte array: $length")
        }
    }

    public fun writeString(value: String) {
        writeBytes(value.encodeToByteArray())
    }

    public inline fun <T> writeVector(value: Collection<T>, block: TlWriter.(T) -> Unit) {
        writeInt(value.size)
        for (item in value) {
            block(item)
        }
    }

    public inline operator fun invoke(block: TlWriter.() -> Unit) {
        block()
    }
}

@Suppress("NOTHING_TO_INLINE")
@Deprecated(DEPRECATION_MESSAGE)
public inline fun <T> TlWriter.write(codec: TlCodec<T>, value: T) {
    codec.encode(this, value)
}

@Deprecated(DEPRECATION_MESSAGE)
public inline fun <T : Any> TlWriter.writeNullable(flag: Int, index: Int, value: T?, block: TlWriter.(T) -> Unit) {
    writeNullable(flag and (1 shl index) != 0, value, block)
}

@Deprecated(DEPRECATION_MESSAGE)
public inline fun <T : Any> TlWriter.writeNullable(flag: Boolean, value: T?, block: TlWriter.(T) -> Unit) {
    if (flag) {
        if (value != null) {
            block(value)
        } else {
            throw IllegalStateException("Nullable value is null, but flag is true")
        }
    }
}

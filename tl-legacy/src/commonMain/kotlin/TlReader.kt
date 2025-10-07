@file:Suppress("DEPRECATION", "NOTHING_TO_INLINE")

package org.ton.tl

import kotlinx.io.*
import kotlinx.io.bytestring.ByteString
import org.ton.tl.constructors.BoolTlCombinator

@Deprecated(DEPRECATION_MESSAGE)
public class TlReader(
    public val input: Source
) {
    public fun readBoolean(): Boolean = BoolTlCombinator.decode(this).value
    public fun readInt(): Int = input.readIntLe()
    public fun readLong(): Long = input.readLongLe()
    public fun readRaw(size: Int): ByteArray = input.readByteArray(size)

    public fun readByteString(size: Int): ByteString {
        return input.readByteString(size)
    }

    public fun readByteString(): ByteString {
        return ByteString(*readBytes())
    }

    public fun readBytes(): ByteArray {
        var resultLength = input.readUByte().toInt()
        var resultAlignedLength: Int
        if (resultLength < 254) {
            resultAlignedLength = resultLength + 1
        } else if (resultLength == 254) {
            resultLength = input.readUByte().toInt() or
                    (input.readUByte().toInt() shl 8) or
                    (input.readUByte().toInt() shl 16)
            resultAlignedLength = resultLength + 4
        } else {
            val resultLengthLong = input.readUByte().toLong() or
                    (input.readUByte().toLong() shl 8) or
                    (input.readUByte().toLong() shl 16) or
                    (input.readUByte().toLong() shl 24) or
                    (input.readUByte().toLong() shl 32) or
                    (input.readUByte().toLong() shl 40) or
                    (input.readUByte().toLong() shl 48)
            if (resultLengthLong > Int.MAX_VALUE) {
                throw IllegalStateException("Too big byte array: $resultLengthLong")
            }
            resultLength = resultLengthLong.toInt()
            resultAlignedLength = resultLength + 8
        }
        val result = input.readByteArray(resultLength)
        while (resultAlignedLength++ % 4 > 0) {
            check(input.readByte() == 0.toByte())
        }
        return result
    }

    public fun readString(): String = readBytes().decodeToString()

    public fun <T> readVector(block: TlReader.() -> T): List<T> {
        val size = readInt()
        return List(size) {
            block()
        }
    }
}

@Deprecated(DEPRECATION_MESSAGE)
public inline operator fun <R> TlReader.invoke(block: TlReader.() -> R): R = block()

@Deprecated(DEPRECATION_MESSAGE)
public inline fun <T> TlReader.read(codec: TlCodec<T>): T = codec.decode(this)

@Deprecated(DEPRECATION_MESSAGE)
public inline fun <E> TlReader.readNullable(flag: Int, index: Int, block: TlReader.() -> E): E? =
    readNullable(flag and (1 shl index) != 0, block)

@Deprecated(DEPRECATION_MESSAGE)
public inline fun <E> TlReader.readNullable(check: Boolean, block: TlReader.() -> E): E? = if (check) block() else null

package org.ton.kotlin.tl.internal

import kotlinx.io.*
import org.ton.kotlin.tl.TL_BOOL_FALSE
import org.ton.kotlin.tl.TL_BOOL_TRUE

internal interface TlReader {
    fun readBoolean(): Boolean
    fun readInt(): Int
    fun readByte(): Byte
    fun readShort(): Short
    fun readChar(): Char
    fun readLong(): Long
    fun readDouble(): Double
    fun readFloat(): Float
    fun readString(): String
    fun readByteArray(byteCount: Int): ByteArray
    fun readByteArray(): ByteArray
}

internal class SourceTlReader(
    private val input: Source
) : TlReader {
    override fun readBoolean(): Boolean {
        return when (val value = input.readIntLe()) {
            TL_BOOL_TRUE -> true
            TL_BOOL_FALSE -> false
            else -> throw IllegalStateException("Expected TL_BOOL_TRUE or TL_BOOL_FALSE, got $value")
        }
    }

    override fun readInt(): Int = input.readIntLe()
    override fun readByte(): Byte = input.readIntLe().toByte()
    override fun readShort(): Short = input.readIntLe().toShort()
    override fun readChar(): Char = input.readIntLe().toChar()
    override fun readLong(): Long = input.readLongLe()

    override fun readDouble(): Double = Double.fromBits(input.readLongLe())

    override fun readFloat(): Float = Float.fromBits(input.readIntLe())

    override fun readString(): String = readByteArray().decodeToString()

    override fun readByteArray(byteCount: Int): ByteArray = input.readByteArray(byteCount)

    override fun readByteArray(): ByteArray {
        val firstByte = input.readUByte().toInt()
        val size: Int
        val lenBytes: Int
        if (firstByte < 254) {
            size = firstByte
            lenBytes = 1
        } else {
            size = input.readUByte().toInt() or
                    (input.readUByte().toInt() shl 8) or
                    (input.readUByte().toInt() shl 16)
            lenBytes = 4
        }

        val result = try {
            input.readByteArray(size)
        } catch (e: Exception) {
            throw IllegalStateException(
                "Failed to read ByteArray of size $size, at: |${
                    input.peek().readByteArray().toHexString()
                }", e
            )
        }
        val paddingBytes = -(lenBytes + size) and 3
        input.skip(paddingBytes.toLong())
        return result
    }
}

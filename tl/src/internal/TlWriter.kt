package org.ton.kotlin.tl.internal

import kotlinx.io.Sink
import kotlinx.io.writeIntLe
import kotlinx.io.writeLongLe
import kotlinx.io.writeUByte
import org.ton.kotlin.tl.TL_BOOL_FALSE
import org.ton.kotlin.tl.TL_BOOL_TRUE

internal interface TlWriter {
    fun writeBoolean(value: Boolean)
    fun writeInt(value: Int)
    fun writeByte(value: Byte)
    fun writeShort(value: Short)
    fun writeChar(value: Char)
    fun writeLong(value: Long)
    fun writeDouble(value: Double)
    fun writeFloat(value: Float)
    fun writeString(value: String)
    fun writeByteArray(byteArray: ByteArray)
    fun writeByteArray(byteArray: ByteArray, offset: Int, length: Int)
}

internal class SinkTlWriter(
    private val sink: Sink
) : TlWriter {
    override fun writeBoolean(value: Boolean) {
        sink.writeIntLe(if (value) TL_BOOL_TRUE else TL_BOOL_FALSE)
    }

    override fun writeInt(value: Int) = sink.writeIntLe(value)

    override fun writeByte(value: Byte) = sink.writeIntLe(value.toInt())

    override fun writeShort(value: Short) = sink.writeIntLe(value.toInt())

    override fun writeChar(value: Char) = sink.writeIntLe(value.code)

    override fun writeLong(value: Long) = sink.writeLongLe(value)

    override fun writeDouble(value: Double) = sink.writeLongLe(value.toBits())

    override fun writeFloat(value: Float) = sink.writeIntLe(value.toBits())

    override fun writeString(value: String) = writeByteArray(value.encodeToByteArray())

    override fun writeByteArray(byteArray: ByteArray) {
        val collectionSize = byteArray.size
        val lenBytes = if (collectionSize < 254) {
            sink.writeUByte(collectionSize.toUByte())
            1
        } else if (collectionSize < (1 shl 24)) {
            sink.writeUByte(254u)
            sink.writeUByte((collectionSize and 255).toUByte())
            sink.writeUByte(((collectionSize shr 8) and 255).toUByte())
            sink.writeUByte((collectionSize shr 16).toUByte())
            4
        } else if (collectionSize < Int.MAX_VALUE) {
            sink.writeUByte(255u)
            sink.writeUByte((collectionSize and 255).toUByte())
            sink.writeUByte(((collectionSize shr 8) and 255).toUByte())
            sink.writeUByte(((collectionSize shr 16) and 255).toUByte())
            sink.writeUByte(((collectionSize shr 24) and 255).toUByte())
            sink.writeByte(0)
            sink.writeByte(0)
            sink.writeByte(0)
            8
        } else {
            error("Too big byte array: $collectionSize")
        }
        // (4 - ((lenBytes + collectionSize) % 4)) % 4
        val paddingBytes = -(lenBytes + collectionSize) and 3
        sink.write(byteArray, 0, collectionSize)
        for (i in 0 until paddingBytes) {
            sink.writeByte(0)
        }
    }

    override fun writeByteArray(byteArray: ByteArray, offset: Int, length: Int) {
        sink.write(byteArray, offset, length)
    }
}

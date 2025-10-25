package org.ton.sdk.cell

import org.ton.sdk.cell.exception.CellOverflowException
import org.ton.sdk.cell.internal.storeIntIntoByteArray
import org.ton.sdk.cell.internal.storeLongIntoByteArray

public interface CellBuilder {

    public fun storeBit(bit: Boolean)

    public fun storeByte(byte: Byte)

    public fun storeInt(int: Int)

    public fun storeShort(short: Short)

    public fun storeLong(long: Long)

    public fun storeInt(value: Int, bits: Int = Int.SIZE_BITS)

    public fun storeLong(value: Long, bits: Int = Long.SIZE_BITS)
}

public inline fun CellBuilder.storeUByte(uByte: UByte): Unit = storeByte(uByte.toByte())
public inline fun CellBuilder.storeUShort(uShort: UShort): Unit = storeShort(uShort.toShort())
public inline fun CellBuilder.storeUInt(uInt: UInt): Unit = storeInt(uInt.toInt())
public inline fun CellBuilder.storeULong(uLong: ULong): Unit = storeLong(uLong.toLong())
public inline fun CellBuilder.storeULong(uLong: ULong, bits: Int): Unit = storeLong(uLong.toLong(), bits)

private class CellBuilderImpl() : CellBuilder {
    private val data = ByteArray(128)
    private var bitLength: Int = 0

    fun storeBitOne() {
        if (bitLength >= Cell.MAX_BIT_LENGHT) {
            throw CellOverflowException()
        }
        val q = bitLength / 8
        val r = bitLength % 8
        data[q] = (data[q].toInt() or (1 shl (7 - r))).toByte()
        bitLength++
    }

    fun storeBitZero() {
        if (bitLength >= Cell.MAX_BIT_LENGHT) {
            throw CellOverflowException()
        }
        bitLength++
    }

    override fun storeBit(bit: Boolean) {
        if (bit) {
            storeBitOne()
        } else {
            storeBitZero()
        }
    }

    override fun storeByte(byte: Byte) {
        if (bitLength + 8 > Cell.MAX_BIT_LENGHT) {
            throw CellOverflowException()
        }
        val q = bitLength / 8
        val r = bitLength % 8
        if (r == 0) {
            // xxxxxxxx
            data[q] = byte
        } else {
            // yyyxxxxx|xxx00000
            data[q] = (data[q].toInt() or ((byte.toInt() and 0xFF) ushr r)).toByte()
            data[q + 1] = ((byte.toInt() and 0xFF) shl (8 - r)).toByte()
        }
        bitLength += 8
    }

    override fun storeInt(int: Int) = storeInt(int, Int.SIZE_BITS)

    override fun storeShort(short: Short) = storeInt(short.toInt(), Short.SIZE_BITS)

    override fun storeLong(long: Long) = storeLong(long, Long.SIZE_BITS)

    override fun storeInt(value: Int, bits: Int) {
        require(bits <= Int.SIZE_BITS) { "Can't store more than ${Int.SIZE_BITS} bits in Int" }
        if (bitLength + bits > Cell.MAX_BIT_LENGHT) {
            throw CellOverflowException()
        }
        storeIntIntoByteArray(data, 0, bitLength, value, bits)
        bitLength += bits
    }

    override fun storeLong(value: Long, bits: Int) {
        require(bits <= Long.SIZE_BITS) { "Can't store more than ${Long.SIZE_BITS} bits in Long" }
        if (bitLength + bits > Cell.MAX_BIT_LENGHT) {
            throw CellOverflowException()
        }
        storeLongIntoByteArray(data, 0, bitLength, value, bits)
        bitLength += bits
    }
}

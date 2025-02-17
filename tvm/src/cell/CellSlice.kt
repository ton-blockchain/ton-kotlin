@file:Suppress("NOTHING_TO_INLINE")

package org.ton.cell

import kotlinx.io.bytestring.ByteString
import org.ton.bigint.*
import org.ton.bitstring.BitString
import org.ton.bitstring.ByteBackedBitString
import org.ton.bitstring.ByteBackedMutableBitString
import org.ton.bitstring.exception.BitStringUnderflowException
import org.ton.cell.exception.CellUnderflowException
import org.ton.kotlin.cell.CellContext
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmStatic

public inline fun CellSlice(bits: BitString, refs: List<Cell> = emptyList()): CellSlice = CellSlice.of(bits, refs)

public interface CellSlice {
    public val bits: BitString
    public val refs: List<Cell>
    public var bitsPosition: Int
    public var refsPosition: Int
    public val remainingBits: Int get() = bits.size - bitsPosition
    public val remainingRefs: Int get() = refs.size - refsPosition

    /**
     * Checks if slice is empty. If not, throws an exception.
     */
    public fun endParse()

    /**
     * Loads the first reference from the slice.
     */
    public fun loadRef(): Cell
    public fun loadRefs(count: Int): List<Cell>

    public fun loadNullableRef(): Cell?

    public fun preloadRef(): Cell
    public fun preloadRefs(count: Int): List<Cell>
    public fun <T> preloadRef(cellSlice: CellSlice.() -> T): T
    public fun preloadRef(offset: Int): Cell

    @Deprecated("use loadBoolean() instead", ReplaceWith("loadBoolean()"))
    public fun loadBit(): Boolean = loadBoolean()

    @Deprecated("use preloadBoolean() instead", ReplaceWith("preloadBoolean()"))
    public fun preloadBit(): Boolean = preloadBoolean()

    public fun loadBoolean(): Boolean
    public fun preloadBoolean(): Boolean

    public fun skipBits(length: Int): CellSlice
    public fun skipRefs(count: Int): CellSlice

    @Deprecated("use loadBitString(length) instead", ReplaceWith("loadBitString(length)"))
    public fun loadBits(length: Int): BitString = loadBitString(length)

    @Deprecated("use preloadBits(length) instead", ReplaceWith("preloadBitString(length)"))
    public fun preloadBits(length: Int): BitString = preloadBitString(length)

    public fun loadBitString(bitCount: Int): BitString
    public fun preloadBitString(bitCount: Int): BitString

    public fun loadByteString(byteCount: Int): ByteString = ByteString(*loadByteArray(byteCount))
    public fun preloadByteString(byteCount: Int): ByteString = ByteString(*preloadByteArray(byteCount))

    public fun loadByteArray(byteCount: Int): ByteArray
    public fun preloadByteArray(byteCount: Int): ByteArray

    public fun loadInt(length: Int): BigInt
    public fun preloadInt(length: Int): BigInt {
        val uint = preloadUInt(length)
        val int = 1.toBigInt() shl (length - 1)
        return if (uint >= int) uint - (int * 2.toBigInt()) else uint
    }

    public fun loadLong(bitCount: Int = Long.SIZE_BITS): Long = loadInt(bitCount).toLong()
    public fun preloadLong(bitCount: Int = Long.SIZE_BITS): Long = preloadInt(bitCount).toLong()

    public fun loadUInt(length: Int): BigInt
    public fun preloadUInt(length: Int): BigInt {
        if (length == 0) return 0.toBigInt()
        val bits = preloadBitString(length)
        val intBits = buildString(length) {
            bits.forEach { bit ->
                if (bit) {
                    append('1')
                } else {
                    append('0')
                }
            }
        }
        return BigInt(intBits, 2)
    }

    public fun loadUInt8(): UByte = loadTinyInt(8).toUByte()
    public fun loadUInt16(): UShort = loadTinyInt(16).toUShort()
    public fun loadUInt32(): UInt = loadTinyInt(32).toUInt()

    @Deprecated("use loadULong() instead", ReplaceWith("loadULong()"))
    public fun loadUInt64(): ULong = loadULong()

    public fun loadULong(bitCount: Int = ULong.SIZE_BITS): ULong = loadTinyInt(64).toULong()

    public fun loadTinyInt(length: Int): Long = loadInt(length).toLong()
    public fun preloadTinyInt(length: Int): Long = preloadInt(length).toLong()

    public fun loadUIntLeq(max: Int): BigInt = loadUInt(Int.SIZE_BITS - max.countLeadingZeroBits())
    public fun preloadUIntLeq(max: Int): BigInt = preloadUInt(Int.SIZE_BITS - max.countLeadingZeroBits())

    public fun loadUIntLes(max: Int): BigInt = loadUIntLeq(max - 1)
    public fun preloadUIntLes(max: Int): BigInt = loadUIntLeq(max - 1)

    public fun isEmpty(): Boolean = bits.isEmpty() && refs.isEmpty()

    public fun countLeadingBits(bit: Boolean): Int =
        bits.countLeadingBits(bitsPosition, bits.size, bit)

    public fun copy(): CellSlice

    public companion object {
        @JvmStatic
        public fun beginParse(cell: Cell): CellSlice {
            return of(cell.bits, cell.refs)
        }

        @Deprecated("Scheduled to remove", level = DeprecationLevel.WARNING)
        @JvmStatic
        public fun of(bits: BitString, refs: List<Cell> = emptyList()): CellSlice {
            return if (bits is ByteBackedBitString) {
                CellSliceByteBackedBitString(bits, refs)
            } else {
                val bitString = ByteBackedMutableBitString.of(bits.size)
                bits.forEachIndexed { index, bit ->
                    bitString[index] = bit
                }
                CellSliceByteBackedBitString(bitString, refs)
            }
        }
    }
}

public inline operator fun <T> CellSlice.invoke(cellSlice: CellSlice.() -> T): T = let(cellSlice)

public inline fun <T> CellSlice.loadRef(context: CellContext = CellContext.EMPTY, block: CellSlice.() -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block(context.loadCell(loadRef()).beginParse())
}

public inline fun <T> CellSlice.parse(block: CellSlice.() -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val result = block()
    endParse()
    return result
}

private class CellSliceByteBackedBitString(
    override val bits: ByteBackedBitString,
    override val refs: List<Cell>,
    override var bitsPosition: Int = 0,
    override var refsPosition: Int = 0
) : CellSlice {
    val data get() = bits.bytes

    fun getBits(offset: Int, length: Int): Byte {
        val index = bitsPosition + offset
        val q = index / 8
        val r = index % 8
        val result = if (r == 0) {
            (data[q].toInt() and 0xFF) shr (8 - length)
        } else if (length <= (8 - r)) {
            ((data[q].toInt() and 0xFF) shr (8 - r - length)) and ((1 shl length) - 1)
        } else {
            var ret = 0
            if (q < data.size) {
                ret = ret or ((data[q].toInt() and 0xFF) shl 8)
            }
            if (q < data.size - 1) {
                ret = ret or (data[q + 1].toInt() and 0xFF)
            }
            (ret shr (8 - r)).toByte().toInt() shr (8 - length)
        }
        return result.toByte()
    }

    fun getByte(offset: Int) = getBits(offset, 8)

    fun getLong(length: Int): ULong {
        if (length == 0) return 0uL
        var value = 0uL
        val bytes = length / 8
        val remainder = length % 8
        repeat(bytes) { i ->
            val byte = getByte(8 * i).toInt() and 0xFF
            value = value or (byte.toULong() shl (8 * (7 - i)))
        }
        if (remainder != 0) {
            val r = getBits(bytes * 8, remainder).toInt() and 0xFF
            value = value or (r.toULong() shl (8 * (7 - bytes) + (8 - remainder)))
        }
        return value shr (64 - length)
    }

    override fun preloadBitString(bitCount: Int): BitString {
        val bits = ByteBackedMutableBitString(bitCount)
        this.bits.copyInto(bits, 0, bitsPosition, bitsPosition + bitCount)
        return bits
    }

    override fun loadByteArray(byteCount: Int): ByteArray {
        val result = preloadByteArray(byteCount)
        bitsPosition += byteCount * 8
        return result
    }

    override fun preloadByteArray(byteCount: Int): ByteArray {
        val array = ByteArray(byteCount)
        val bits = ByteBackedMutableBitString(array, byteCount * 8)
        this.bits.copyInto(bits, 0, bitsPosition, bitsPosition + byteCount * 8)
        return array
    }

    override fun preloadUInt(length: Int): BigInt {
        return when {
            length == 0 -> 0.toBigInt()
            length > 64 -> super.preloadUInt(length)
            length == 8 -> {
                val byte = getByte(0).toInt() and 0xFF
                byte.toBigInt()
            }

            else -> {
                val value = getLong(length)
                if (value > Long.MAX_VALUE.toULong()) {
                    BigInt(value.toString(), 10)
                } else {
                    value.toLong().toBigInt()
                }
            }
        }
    }

    override fun preloadInt(length: Int): BigInt {
        return when {
            length == 0 -> 0.toBigInt()
            length > 64 -> super.preloadInt(length)
            else -> {
                val uint = getLong(length).toLong()
                val int = 1L shl (length - 1)
                if (uint >= int) {
                    (uint - (int * 2)).toBigInt()
                } else {
                    uint.toBigInt()
                }
            }
        }
    }

    override fun preloadTinyInt(length: Int): Long {
        return when {
            length == 0 -> 0
            length <= 64 -> {
                val uint = getLong(length).toLong()
                val int = 1L shl (length - 1)
                if (uint >= int) {
                    uint - (int * 2)
                } else {
                    uint
                }
            }

            else -> throw IllegalArgumentException("expected length in 0..64, actual: $length")
        }
    }

    override fun endParse() {
        check(bitsPosition >= bits.size) { "bitsPosition: $bitsPosition != bits.length: ${bits.size}" }
        check(refsPosition >= refs.size) { "refsPosition: $refsPosition != refs.size: ${refs.size}" }
    }

    override fun loadRef(): Cell {
        checkRefsOverflow()
        val cell = preloadRef()
        refsPosition++
        return cell
    }

    override fun loadNullableRef(): Cell? {
        return if (loadBoolean()) {
            loadRef()
        } else {
            null
        }
    }

    override fun loadRefs(count: Int): List<Cell> = List(count) { loadRef() }

    override fun preloadRef(): Cell = refs[refsPosition]

    override fun <T> preloadRef(cellSlice: CellSlice.() -> T): T {
        val slice = preloadRef().beginParse()
        return cellSlice(slice)
    }

    override fun preloadRef(offset: Int): Cell {
        return refs[refsPosition + offset]
    }

    override fun preloadRefs(count: Int): List<Cell> = List(refsPosition + count) { refs[it] }

    override fun loadBoolean(): Boolean {
        val bit = preloadBoolean()
        bitsPosition++
        return bit
    }

    override fun preloadBoolean(): Boolean = try {
        bits[bitsPosition]
    } catch (e: BitStringUnderflowException) {
        throw CellUnderflowException(e)
    }

    override fun skipBits(length: Int): CellSlice = apply {
        bitsPosition += length
    }

    override fun skipRefs(count: Int): CellSlice {
        refsPosition += count
        return this
    }

    override fun loadBitString(bitCount: Int): BitString {
        val bitString = preloadBitString(bitCount)
        bitsPosition += bitCount
        return bitString
    }

    override fun loadInt(length: Int): BigInt {
        val int = preloadInt(length)
        bitsPosition += length
        return int
    }

    override fun loadTinyInt(length: Int): Long {
        val tinyInt = preloadTinyInt(length)
        bitsPosition += length
        return tinyInt
    }

    override fun loadUInt(length: Int): BigInt {
        val uint = preloadUInt(length)
        bitsPosition += length
        return uint
    }

    protected fun checkBitsOverflow(length: Int) {
        val remaining = bits.size - bitsPosition
        require(length <= remaining) {
            "Bits overflow. Can't load $length bits. $remaining bits left."
        }
    }

    protected fun checkRefsOverflow() {
        val remaining = 4 - refsPosition
        require(1 <= remaining) {
            "Refs overflow. Can't load ref. $remaining refs left."
        }
    }

    override fun copy(): CellSlice {
        return CellSliceByteBackedBitString(
            bits.toBitString(),
            refs,
            bitsPosition,
            refsPosition
        )
    }

    override fun toString(): String = buildString {
        append("x")
        append(bits.toString())
        if (refs.isNotEmpty()) {
            append(",")
            append(refs.size)
        }
    }
}

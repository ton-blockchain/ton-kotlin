package org.ton.sdk.bitstring

import kotlinx.io.bytestring.ByteString
import org.ton.sdk.bigint.toBigInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BitStringBuilderTest {
    @Test
    fun bitStringBuilder() {
        val builder = BitStringBuilder()
        builder.writeBinary("00101101100")
        assertEquals("x{2D9_}", builder.toBitString().toString())
    }

    @Test
    fun testWriteSingleBit() {
        val builder = BitStringBuilder()
        builder.writeBit(true)
        assertEquals("x{C_}", builder.toBitString().toString())

        val builder2 = BitStringBuilder()
        builder2.writeBit(false)
        assertEquals("x{4_}", builder2.toBitString().toString())
    }

    @Test
    fun testWriteMultipleBits() {
        val builder = BitStringBuilder()
        builder.writeBit(true)
        builder.writeBit(false)
        builder.writeBit(true)
        builder.writeBit(true)
        assertEquals("x{B}", builder.toBitString().toString())
    }

    @Test
    fun testWriteByte() {
        val builder = BitStringBuilder()
        builder.writeByte(0xFF.toByte())
        assertEquals("x{FF}", builder.toBitString().toString())

        val builder2 = BitStringBuilder()
        builder2.writeByte(0x00.toByte())
        assertEquals("x{00}", builder2.toBitString().toString())

        val builder3 = BitStringBuilder()
        builder3.writeByte(0xAB.toByte())
        assertEquals("x{AB}", builder3.toBitString().toString())
    }

    @Test
    fun testWriteByteNonAligned() {
        val builder = BitStringBuilder()
        builder.writeBit(true)
        builder.writeByte(0xFF.toByte())
        assertEquals("x{FFC_}", builder.toBitString().toString())

        val builder2 = BitStringBuilder()
        builder2.writeBinary("0101")
        builder2.writeByte(0xAB.toByte())
        assertEquals("x{5AB}", builder2.toBitString().toString())
    }

    @Test
    fun testWriteShort() {
        val builder = BitStringBuilder()
        builder.writeShort(0x1234.toShort())
        assertEquals("x{1234}", builder.toBitString().toString())

        val builder2 = BitStringBuilder()
        builder2.writeShort((-1).toShort())
        assertEquals("x{FFFF}", builder2.toBitString().toString())
    }

    @Test
    fun testWriteInt() {
        val builder = BitStringBuilder()
        builder.writeInt(0x12345678)
        assertEquals("x{12345678}", builder.toBitString().toString())

        val builder2 = BitStringBuilder()
        builder2.writeInt(-1)
        assertEquals("x{FFFFFFFF}", builder2.toBitString().toString())

        val builder3 = BitStringBuilder()
        builder3.writeInt(0)
        assertEquals("x{00000000}", builder3.toBitString().toString())
    }

    @Test
    fun testWriteLong() {
        val builder = BitStringBuilder()
        builder.writeLong(0x123456789ABCDEF0L)
        assertEquals("x{123456789ABCDEF0}", builder.toBitString().toString())

        val builder2 = BitStringBuilder()
        builder2.writeLong(-1L)
        assertEquals("x{FFFFFFFFFFFFFFFF}", builder2.toBitString().toString())
    }

    @Test
    fun testWriteIntWithBits() {
        val builder = BitStringBuilder()
        builder.writeUInt(0b1111, 4)
        assertEquals("x{F}", builder.toBitString().toString())

        val builder2 = BitStringBuilder()
        builder2.writeUInt(0b10101010, 8)
        assertEquals("x{AA}", builder2.toBitString().toString())

        val builder3 = BitStringBuilder()
        builder3.writeUInt(0x1F, 5)
        assertEquals("x{FC_}", builder3.toBitString().toString())

        val builder4 = BitStringBuilder()
        builder4.writeUInt(1, 1)
        assertEquals("x{C_}", builder4.toBitString().toString())

        val builder5 = BitStringBuilder()
        builder5.writeUInt(0, 1)
        assertEquals("x{4_}", builder5.toBitString().toString())
    }

    @Test
    fun testWriteLongWithBits() {
        val builder = BitStringBuilder()
        builder.writeULong(0xFFFFFFFFFFL, 40)
        assertEquals("x{FFFFFFFFFF}", builder.toBitString().toString())

        val builder2 = BitStringBuilder()
        builder2.writeULong(0x1L, 1)
        assertEquals("x{C_}", builder2.toBitString().toString())

        val builder3 = BitStringBuilder()
        builder3.writeULong(0xABCDEF, 24)
        assertEquals("x{ABCDEF}", builder3.toBitString().toString())
    }

    @Test
    fun testWriteUBigInt() {
        val builder = BitStringBuilder()
        builder.writeUBigInt(255.toBigInt(), 8)
        assertEquals("x{FF}", builder.toBitString().toString())

        val builder2 = BitStringBuilder()
        builder2.writeUBigInt(15.toBigInt(), 4)
        assertEquals("x{F}", builder2.toBitString().toString())

        val builder3 = BitStringBuilder()
        val bigValue = "18446744073709551615".toBigInt() // Max uint64
        builder3.writeUBigInt(bigValue, 64)
        assertEquals("x{FFFFFFFFFFFFFFFF}", builder3.toBitString().toString())

        val builder4 = BitStringBuilder()
        val veryBigValue = "184467440173709551615".toBigInt()
        builder4.writeUBigInt(veryBigValue, 68)
        assertEquals("x{9FFFFFF7CD39467FF}", builder4.toBitString().toString())
    }

    @Test
    fun testWriteBitString() {
        val builder = BitStringBuilder()
        val bitString = BitString("x{AB}")
        builder.write(bitString, 0, bitString.size)
        assertEquals("x{AB}", builder.toBitString().toString())

        val builder2 = BitStringBuilder()
        builder2.writeBit(true)
        builder2.write(bitString, 0, bitString.size)
        assertEquals("x{D5C_}", builder2.toBitString().toString())
    }

    @Test
    fun testWriteBitStringPartial() {
        val builder = BitStringBuilder()
        val bitString = BitString("x{ABCD}")
        builder.write(bitString, 0, 8)
        assertEquals("x{AB}", builder.toBitString().toString())

        val builder2 = BitStringBuilder()
        builder2.write(bitString, 4, 12)
        assertEquals("x{BC}", builder2.toBitString().toString())
    }

    @Test
    fun testWriteByteArray() {
        val builder = BitStringBuilder()
        val bytes = byteArrayOf(0xAB.toByte(), 0xCD.toByte())
        builder.write(bytes, 0, bytes.size)
        assertEquals("x{ABCD}", builder.toBitString().toString())

        val builder2 = BitStringBuilder()
        builder2.writeBit(true)
        builder2.write(bytes, 0, bytes.size)
        assertEquals("x{D5E6C_}", builder2.toBitString().toString())
    }

    @Test
    fun testWriteByteArrayPartial() {
        val builder = BitStringBuilder()
        val bytes = byteArrayOf(0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte())
        builder.write(bytes, 1, 3)
        assertEquals("x{CDEF}", builder.toBitString().toString())
    }

    @Test
    fun testWriteByteString() {
        val builder = BitStringBuilder()
        val byteString = ByteString(0xAB.toByte(), 0xCD.toByte())
        builder.write(byteString, 0, byteString.size)
        assertEquals("x{ABCD}", builder.toBitString().toString())
    }

    @Test
    fun testEmpty() {
        val builder = BitStringBuilder()
        assertEquals("x{}", builder.toBitString().toString())
    }

    @Test
    fun testMixedWrites() {
        val builder = BitStringBuilder()
        builder.writeBit(true)
        builder.writeByte(0xFF.toByte())
        builder.writeUInt(0x12, 5)
        builder.writeBit(false)
        val result = builder.toBitString()
        assertEquals(1 + 8 + 5 + 1, result.size)
    }

    @Test
    fun testComplexSequence() {
        val builder = BitStringBuilder()
        builder.writeBinary("1010")
        builder.writeByte(0xBC.toByte())
        builder.writeBinary("11")
        assertEquals("x{ABCE_}", builder.toBitString().toString())
    }

    @Test
    fun testBitStringGet() {
        val builder = BitStringBuilder()
        builder.writeBinary("10101100")
        val bitString = builder.toBitString()
        assertTrue(bitString[0])
        assertFalse(bitString[1])
        assertTrue(bitString[2])
        assertFalse(bitString[3])
        assertTrue(bitString[4])
        assertTrue(bitString[5])
        assertFalse(bitString[6])
        assertFalse(bitString[7])
    }

    @Test
    fun testToString() {
        val builder = BitStringBuilder()
        builder.writeByte(0xAB.toByte())
        val str = builder.toString()
        assertTrue(str.contains("bitLength=8"))
        assertTrue(str.contains("x{AB}"))
    }

    @Test
    fun testMultipleBytes() {
        val builder = BitStringBuilder()
        for (i in 0..15) {
            builder.writeByte(i.toByte())
        }
        assertEquals("x{000102030405060708090A0B0C0D0E0F}", builder.toBitString().toString())
    }

    @Test
    fun testLargeData() {
        val builder = BitStringBuilder()
        repeat(32) {
            builder.writeInt(0x12345678)
        }
        val result = builder.toBitString()
        assertEquals(32 * 32, result.size)
    }

    @Test
    fun testNonByteAlignedSequence() {
        val builder = BitStringBuilder()
        builder.writeBinary("101")
        builder.writeBinary("110")
        builder.writeBinary("01")
        assertEquals("x{B9}", builder.toBitString().toString())
    }

    @Test
    fun testWriteIntVariousBitLengths() {
        for (bits in 1..32) {
            val builder = BitStringBuilder()
            builder.writeInt(-1, bits)
            val result = builder.toBitString()
            assertEquals(bits, result.size)
            for (i in 0 until bits) {
                assertTrue(result[i], "Bit $i should be true for bits=$bits")
            }
        }
    }

    @Test
    fun testWriteLongVariousBitLengths() {
        for (bits in listOf(1, 8, 16, 32, 48, 64)) {
            val builder = BitStringBuilder()
            builder.writeLong(-1L, bits)
            val result = builder.toBitString()
            assertEquals(bits, result.size)
            for (i in 0 until bits) {
                assertTrue(result[i], "Bit $i should be true for bits=$bits")
            }
        }
    }

    @Test
    fun testBitPatterns() {
        val builder = BitStringBuilder()
        builder.writeBinary("00000000")
        assertEquals("x{00}", builder.toBitString().toString())

        val builder2 = BitStringBuilder()
        builder2.writeBinary("11111111")
        assertEquals("x{FF}", builder2.toBitString().toString())

        val builder3 = BitStringBuilder()
        builder3.writeBinary("01010101")
        assertEquals("x{55}", builder3.toBitString().toString())

        val builder4 = BitStringBuilder()
        builder4.writeBinary("10101010")
        assertEquals("x{AA}", builder4.toBitString().toString())
    }

    @Test
    fun testZeroLengthWrites() {
        val builder = BitStringBuilder()
        builder.writeInt(0xFF, 0)
        assertEquals("x{}", builder.toBitString().toString())

        val builder2 = BitStringBuilder()
        builder2.writeLong(0xFFFFL, 0)
        assertEquals("x{}", builder2.toBitString().toString())
    }

    @Test
    fun testSingleBitInt() {
        val builder = BitStringBuilder()
        builder.writeUInt(1, 1)
        assertEquals("x{C_}", builder.toBitString().toString())

        val builder2 = BitStringBuilder()
        builder2.writeUInt(0, 1)
        assertEquals("x{4_}", builder2.toBitString().toString())
    }

    @Test
    fun testBigInt() {
        val builder = BitStringBuilder()
        builder.writeUBigInt("-1000000000000000000000000239".toBigInt(), 91)
        assertEquals("x{989A386C05EFF862FFFFE23_}", builder.toBitString().toString())
    }

    private fun BitStringBuilder.writeBinary(binary: String) {
        for (ch in binary) {
            writeBit(ch != '0')
        }
    }
}

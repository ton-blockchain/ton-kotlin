package org.ton.sdk.cell.internal

import org.ton.sdk.bigint.or
import org.ton.sdk.bigint.shl
import org.ton.sdk.bigint.toBigInt
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class StoreBigIntTest {

    @Test
    fun `test storeBigInt with zero bits`() {
        val dest = ByteArray(10) { 0xFF.toByte() }
        storeBigIntIntoByteArray(dest, 0, 0, 12345.toBigInt(), 0)
        // Array should not be modified
        assertContentEquals(ByteArray(10) { 0xFF.toByte() }, dest)
    }

    @Test
    fun `test storeBigInt byte-aligned - 16 bits`() {
        val dest = ByteArray(10) { 0 }
        val v = 0xABCD.toBigInt()
        storeBigIntIntoByteArray(dest, 0, 0, v, 16)
        assertEquals(0xAB.toByte(), dest[0])
        assertEquals(0xCD.toByte(), dest[1])
        assertEquals(0, dest[2])
    }

    @Test
    fun `test storeBigInt byte-aligned - 72 bits`() {
        val dest = ByteArray(16) { 0 }
        val v = (0x0123456789ABCDEFL.toBigInt() shl 8) or 0x00.toBigInt()
        // v = 0x0123456789ABCDEF00 -> write exactly 72 bits (9 bytes)
        storeBigIntIntoByteArray(dest, 0, 0, v, 72)
        val expected = byteArrayOf(
            0x01.toByte(),
            0x23.toByte(),
            0x45.toByte(),
            0x67.toByte(),
            0x89.toByte(),
            0xAB.toByte(),
            0xCD.toByte(),
            0xEF.toByte(),
            0x00.toByte()
        )
        assertContentEquals(expected, dest.copyOfRange(0, 9))
    }

    @Test
    fun `test storeBigInt with bit offset 1`() {
        val dest = ByteArray(4) { 0 }
        storeBigIntIntoByteArray(dest, 0, 1, 0xFF.toBigInt(), 8)
        // Expected: 01111111 10000000
        assertEquals(0x7F.toByte(), dest[0])
        assertEquals(0x80.toByte(), dest[1])
    }

    @Test
    fun `test storeBigInt with bit offset 4`() {
        val dest = ByteArray(4) { 0 }
        storeBigIntIntoByteArray(dest, 0, 4, 0xAB.toBigInt(), 8)
        // Expected: 00001010 10110000
        assertEquals(0x0A.toByte(), dest[0])
        assertEquals(0xB0.toByte(), dest[1])
    }

    @Test
    fun `test storeBigInt with bit offset 7`() {
        val dest = ByteArray(4) { 0 }
        storeBigIntIntoByteArray(dest, 0, 7, 0xFF.toBigInt(), 8)
        // Expected: 00000001 11111110
        assertEquals(0x01.toByte(), dest[0])
        assertEquals(0xFE.toByte(), dest[1])
    }

    @Test
    fun `test storeBigInt preserves existing bits`() {
        val dest = ByteArray(4) { 0xFF.toByte() }
        // Writing 0x00 with offset 4 bits, 4 bits length
        storeBigIntIntoByteArray(dest, 0, 4, 0x00.toBigInt(), 4)
        // First 4 bits should remain 1111, next 4 - 0000
        assertEquals(0xF0.toByte(), dest[0])
        assertEquals(0xFF.toByte(), dest[1]) // not affected
    }

    @Test
    fun `test storeBigInt negative aligned`() {
        val dest = ByteArray(3) { 0 }
        storeBigIntIntoByteArray(dest, 0, 0, (-1).toBigInt(), 9)
        // Expect 9 ones: FF 80
        assertEquals(0xFF.toByte(), dest[0])
        assertEquals(0x80.toByte(), dest[1])
    }

    @Test
    fun `test storeBigInt vs storeLong for up to 64 bits`() {
        repeat(16) { bits ->
            val b = bits + 1 // 1..16
            val dest1 = ByteArray(16)
            val dest2 = ByteArray(16)
            val v = (0xFFFFuL and ((1uL shl b) - 1uL)).toLong()
            storeLongIntoByteArray(dest1, 0, 3, v, b)
            storeBigIntIntoByteArray(dest2, 0, 3, v.toBigInt(), b)
            assertContentEquals(dest1, dest2)
        }

        // Random 64-bit checks
        val longValues = longArrayOf(0L, 1L, -1L, Long.MIN_VALUE, Long.MAX_VALUE, 0x0123456789ABCDEFL)
        for (v in longValues) {
            for (b in intArrayOf(1, 7, 8, 9, 15, 16, 31, 32, 47, 48, 63, 64)) {
                val dest1 = ByteArray(16)
                val dest2 = ByteArray(16)
                storeLongIntoByteArray(dest1, 1, 5, v, b)
                storeBigIntIntoByteArray(dest2, 1, 5, v.toBigInt(), b)
                assertContentEquals(dest1, dest2)
            }
        }
    }
}

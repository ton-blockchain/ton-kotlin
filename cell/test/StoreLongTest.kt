package org.ton.sdk.cell.internal

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class StoreLongTest {

    @Test
    fun `test storeLong with zero bits`() {
        val dest = ByteArray(10) { 0xFF.toByte() }
        storeLong(dest, 0, 0, 0x123456789ABCDEF0L, 0)
        // Array should not be modified
        assertContentEquals(ByteArray(10) { 0xFF.toByte() }, dest)
    }

    @Test
    fun `test storeLong byte-aligned - 8 bits`() {
        val dest = ByteArray(10) { 0 }
        storeLong(dest, 0, 0, 0xAB, 8)
        assertEquals(0xAB.toByte(), dest[0])
        assertEquals(0, dest[1])
    }

    @Test
    fun `test storeLong byte-aligned - 16 bits`() {
        val dest = ByteArray(10) { 0 }
        storeLong(dest, 0, 0, 0xABCD, 16)
        assertEquals(0xAB.toByte(), dest[0])
        assertEquals(0xCD.toByte(), dest[1])
        assertEquals(0, dest[2])
    }

    @Test
    fun `test storeLong byte-aligned - 32 bits`() {
        val dest = ByteArray(10) { 0 }
        storeLong(dest, 0, 0, 0x12345678, 32)
        assertEquals(0x12.toByte(), dest[0])
        assertEquals(0x34.toByte(), dest[1])
        assertEquals(0x56.toByte(), dest[2])
        assertEquals(0x78.toByte(), dest[3])
    }

    @Test
    fun `test storeLong byte-aligned - 64 bits`() {
        val dest = ByteArray(10) { 0 }
        storeLong(dest, 0, 0, 0x123456789ABCDEF0L, 64)
        assertEquals(0x12.toByte(), dest[0])
        assertEquals(0x34.toByte(), dest[1])
        assertEquals(0x56.toByte(), dest[2])
        assertEquals(0x78.toByte(), dest[3])
        assertEquals(0x9A.toByte(), dest[4])
        assertEquals(0xBC.toByte(), dest[5])
        assertEquals(0xDE.toByte(), dest[6])
        assertEquals(0xF0.toByte(), dest[7])
    }

    @Test
    fun `test storeLong byte-aligned with offset`() {
        val dest = ByteArray(10) { 0 }
        storeLong(dest, 2, 0, 0xABCD, 16)
        assertEquals(0, dest[0])
        assertEquals(0, dest[1])
        assertEquals(0xAB.toByte(), dest[2])
        assertEquals(0xCD.toByte(), dest[3])
    }

    @Test
    fun `test storeLong with bit offset 1`() {
        val dest = ByteArray(10) { 0 }
        // Writing 0xFF (11111111) with 1 bit shift
        storeLong(dest, 0, 1, 0xFF, 8)
        // Expected: 01111111 10000000
        assertEquals(0x7F.toByte(), dest[0])
        assertEquals(0x80.toByte(), dest[1])
    }

    @Test
    fun `test storeLong with bit offset 4`() {
        val dest = ByteArray(10) { 0 }
        // Writing 0xAB (10101011) with 4 bit shift
        storeLong(dest, 0, 4, 0xAB, 8)
        // Expected: 00001010 10110000
        assertEquals(0x0A.toByte(), dest[0])
        assertEquals(0xB0.toByte(), dest[1])
    }

    @Test
    fun `test storeLong with bit offset 7`() {
        val dest = ByteArray(10) { 0 }
        // Writing 0xFF with 7 bit shift
        storeLong(dest, 0, 7, 0xFF, 8)
        // Expected: 00000001 11111110
        assertEquals(0x01.toByte(), dest[0])
        assertEquals(0xFE.toByte(), dest[1])
    }

    @Test
    fun `test storeLong preserves existing bits`() {
        val dest = ByteArray(10) { 0xFF.toByte() }
        // Writing 0x00 with offset 4 bits, 4 bits length
        storeLong(dest, 0, 4, 0x00, 4)
        // First 4 bits should remain 1111, next 4 - 0000
        assertEquals(0xF0.toByte(), dest[0])
        assertEquals(0xFF.toByte(), dest[1]) // not affected
    }

    @Test
    fun `test storeLong single bit at offset 0`() {
        val dest = ByteArray(10) { 0 }
        storeLong(dest, 0, 0, 1, 1)
        assertEquals(0x80.toByte(), dest[0])
    }

    @Test
    fun `test storeLong single bit at offset 3`() {
        val dest = ByteArray(10) { 0 }
        storeLong(dest, 0, 3, 1, 1)
        assertEquals(0x10.toByte(), dest[0])
    }

    @Test
    fun `test storeLong single bit at offset 7`() {
        val dest = ByteArray(10) { 0 }
        storeLong(dest, 0, 7, 1, 1)
        assertEquals(0x01.toByte(), dest[0])
    }

    @Test
    fun `test storeLong 7 bits byte-aligned`() {
        val dest = ByteArray(10) { 0 }
        storeLong(dest, 0, 0, 0x7F, 7)
        assertEquals(0xFE.toByte(), dest[0])
        assertEquals(0x00.toByte(), dest[1])
    }

    @Test
    fun `test storeLong 63 bits`() {
        val dest = ByteArray(10) { 0 }
        val value = 0x7FFFFFFFFFFFFFFFL // 63 one bits
        storeLong(dest, 0, 0, value, 63)
        assertEquals(0xFF.toByte(), dest[0])
        assertEquals(0xFF.toByte(), dest[1])
        assertEquals(0xFF.toByte(), dest[2])
        assertEquals(0xFF.toByte(), dest[3])
        assertEquals(0xFF.toByte(), dest[4])
        assertEquals(0xFF.toByte(), dest[5])
        assertEquals(0xFF.toByte(), dest[6])
        assertEquals(0xFE.toByte(), dest[7])
    }

    @Test
    fun `test storeLong with negative value`() {
        val dest = ByteArray(10) { 0 }
        storeLong(dest, 0, 0, -1L, 64)
        // -1L = 0xFFFFFFFFFFFFFFFF
        for (i in 0..7) {
            assertEquals(0xFF.toByte(), dest[i])
        }
    }

    @Test
    fun `test storeLong negative value with limited bits`() {
        val dest = ByteArray(10) { 0 }
        // Writing the least significant 8 bits from -1
        storeLong(dest, 0, 0, -1L, 8)
        assertEquals(0xFF.toByte(), dest[0])
    }

    @Test
    fun `test storeLong crossing byte boundary with offset`() {
        val dest = ByteArray(10) { 0 }
        // 16 bits with offset 4 - crosses 3 bytes
        storeLong(dest, 0, 4, 0xABCD, 16)
        assertEquals(0x0A.toByte(), dest[0])
        assertEquals(0xBC.toByte(), dest[1])
        assertEquals(0xD0.toByte(), dest[2])
    }

    @Test
    fun `test storeLong maximum crossing - 64 bits with offset 7`() {
        val dest = ByteArray(10) { 0 }
        storeLong(dest, 0, 7, 0xFF, 8)
        assertEquals(0x01.toByte(), dest[0])
        assertEquals(0xFE.toByte(), dest[1])
    }

    @Test
    fun `test storeLong preserves surrounding bits`() {
        val dest = byteArrayOf(
            0b11110000.toByte(),
            0b00000000.toByte(),
            0b00001111.toByte()
        )
        // Writing 4 bits (1010) starting from offset 4 of the first byte
        storeLong(dest, 0, 4, 0b1010, 4)
        // Expected: 11111010 00000000 00001111
        assertEquals(0b11111010.toByte(), dest[0])
        assertEquals(0b00000000.toByte(), dest[1])
        assertEquals(0b00001111.toByte(), dest[2])
    }

    @Test
    fun `test storeLong with all zeros`() {
        val dest = ByteArray(10) { 0xFF.toByte() }
        storeLong(dest, 0, 0, 0, 8)
        assertEquals(0x00.toByte(), dest[0])
        assertEquals(0xFF.toByte(), dest[1])
    }

    @Test
    fun `test storeLong multiple sequential writes`() {
        val dest = ByteArray(10) { 0 }
        storeLong(dest, 0, 0, 0xFF, 8)
        storeLong(dest, 1, 0, 0xAA, 8)
        storeLong(dest, 2, 0, 0x55, 8)
        assertEquals(0xFF.toByte(), dest[0])
        assertEquals(0xAA.toByte(), dest[1])
        assertEquals(0x55.toByte(), dest[2])
    }

    @Test
    fun `test storeLong pattern 0x5555555555555555`() {
        val dest = ByteArray(10) { 0 }
        storeLong(dest, 0, 0, 0x5555555555555555L, 64)
        for (i in 0..7) {
            assertEquals(0x55.toByte(), dest[i])
        }
    }

    @Test
    fun `test storeLong pattern 0xAAAAAAAAAAAAAAAA`() {
        val dest = ByteArray(10) { 0 }
        storeLong(dest, 0, 0, 0xAAAAAAAAAAAAAAAAuL.toLong(), 64)
        for (i in 0..7) {
            assertEquals(0xAA.toByte(), dest[i])
        }
    }

    @Test
    fun `test storeLong partial byte at end with bit offset`() {
        val dest = ByteArray(10) { 0 }
        // 5 bits with offset 2
        storeLong(dest, 0, 2, 0b11111, 5)
        // Expected: 00111110 (bits 2-6 are filled)
        assertEquals(0b00111110.toByte(), dest[0])
    }

    @Test
    fun `test storeLong 32 bits with offset 1`() {
        val dest = ByteArray(10) { 0 }
        storeLong(dest, 0, 1, 0x12345678, 32)
        assertEquals(0x09.toByte(), dest[0]) // 0001001
        assertEquals(0x1A.toByte(), dest[1])
        assertEquals(0x2B.toByte(), dest[2])
        assertEquals(0x3C.toByte(), dest[3])
        assertEquals(0x00.toByte(), dest[4])
    }

    private fun ByteArray.toBinaryString(): String {
        return this.joinToString(" ") { byte ->
            byte.toUByte().toString(2).padStart(8, '0')
        }
    }
}

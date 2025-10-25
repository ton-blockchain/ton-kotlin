package org.ton.sdk.bitstring

import kotlin.random.Random
import kotlin.test.*

class BitStringTest {
    @Test
    fun `test BitString creation from hex string`() {
        // Test basic hex strings
        assertBitString("0", "4_")
        assertBitString("00", "2_")
        assertBitString("000", "1_")
        assertBitString("0000", "0")
        assertBitString("00000", "04_")
        assertBitString("000000", "02_")
        assertBitString("0000000", "01_")
        assertBitString("00000000", "00")

        assertBitString("1", "C_")
        assertBitString("11", "E_")
        assertBitString("111", "F_")
        assertBitString("1111", "F")
        assertBitString("11111", "FC_")
        assertBitString("111111", "FE_")
        assertBitString("1111111", "FF_")
        assertBitString("11111111", "FF")

        assertBitString("01", "6_")
        assertBitString("001", "3_")
        assertBitString("0001", "1")
        assertBitString("00001", "0C_")
        assertBitString("000001", "06_")
        assertBitString("0000001", "03_")
        assertBitString("00000001", "01")

        assertBitString("10001010", "8A")
        assertBitString("100010", "8A_")
        assertBitString("00101101100", "2D9_")
    }

//    @Test
//    fun `test BitString creation from ByteArray`() {
//        // Test ByteArray constructor with completion tag format
//        // Format: bitstring is padded with 1 followed by zeros to fill the last byte
//
//        // Example 1: [0x8A, 0x80] = 10001010 10000000
//        // The trailing 1 at position 8 is the marker, so we have 6 bits: 100010
//        val bytes1 = byteArrayOf(0x8A.toByte(), 0x80.toByte())
//        val bs1 = BitString(bytes1)
//        assertEquals("8A_", bs1.toString())
//        assertEquals(6, bs1.size)
//
//        // Example 2: [0xFF] = 11111111
//        // The trailing 1 at position 7 is the marker, so we have 7 bits: 1111111
//        val bytes2 = byteArrayOf(0xFF.toByte())
//        val bs2 = BitString(bytes2)
//        assertEquals("FF_", bs2.toString())
//        assertEquals(7, bs2.size)
//
//        // Example 3: [0xFE] = 11111110
//        // The trailing 1 at position 6 is the marker, so we have 6 bits: 111111
//        val bytes3 = byteArrayOf(0xFE.toByte())
//        val bs3 = BitString(bytes3)
//        assertEquals("FE_", bs3.toString())
//        assertEquals(6, bs3.size)
//
//        // Example 4: Full byte with marker in next byte [0xCA, 0xFE, 0x80]
//        // 11001010 11111110 10000000 -> marker at bit 16, so 16 bits: CAFE
//        val bytes4 = byteArrayOf(0xCA.toByte(), 0xFE.toByte(), 0x80.toByte())
//        val bs4 = BitString(bytes4)
//        assertEquals("CAFE", bs4.toString())
//        assertEquals(16, bs4.size)
//    }

    @Test
    fun `test BitString get operator`() {
        val bs = BitString("8A") // 10001010
        assertEquals(true, bs[0])
        assertEquals(false, bs[1])
        assertEquals(false, bs[2])
        assertEquals(false, bs[3])
        assertEquals(true, bs[4])
        assertEquals(false, bs[5])
        assertEquals(true, bs[6])
        assertEquals(false, bs[7])
    }

    @Test
    fun `test BitString get operator bounds`() {
        val bs = BitString("F") // 1111 (4 bits)
        assertEquals(4, bs.size)
        assertTrue(bs[0])
        assertTrue(bs[3])

        assertFailsWith<IndexOutOfBoundsException> {
            bs[-1]
        }
        assertFailsWith<IndexOutOfBoundsException> {
            bs[4]
        }
    }

    @Test
    fun `test BitString equality`() {
        val bs1 = BitString("CAFE")
        val bs2 = BitString("CAFE")
        val bs3 = BitString("BABE")

        assertEquals(bs1, bs2)
        assertNotEquals(bs1, bs3)
        assertEquals(bs1.hashCode(), bs2.hashCode())
    }

    @Test
    fun `test BitString equality with same content`() {
        val bs1 = BitString("8A_")
        val bs2 = BitString("8A_")
        assertEquals(bs1, bs2)
        assertEquals(bs1.hashCode(), bs2.hashCode())
    }

    @Test
    fun `test BitString substring`() {
        val bs = BitString("CAFEBABE")
        assertEquals("CAFE", bs.substring(0, 16).toString())
        assertEquals("BABE", bs.substring(16, 32).toString())
        assertEquals("FE", bs.substring(8, 16).toString())
        assertEquals("CA", bs.substring(0, 8).toString())
    }

    @Test
    fun `test BitString substring with incomplete bytes`() {
        val bs = BitString("8A") // 10001010
        assertEquals("8", bs.substring(0, 4).toString()) // 1000
        assertEquals("A", bs.substring(4, 8).toString()) // 1010
        assertEquals("8A", bs.substring(0, 8).toString()) // 10001010
        assertEquals("8A_", bs.substring(0, 6).toString()) // 100010
        assertEquals("9_", bs.substring(0, 3).toString()) // 100
        assertEquals("B_", bs.substring(4, 7).toString()) // 101
        assertEquals("A_", bs.substring(4, 6).toString()) // 10
        assertEquals("A_", bs.substring(0, 2).toString()) // 10
    }

    @Test
    fun `test BitString substring bounds`() {
        val bs = BitString("CAFE")
        assertEquals(16, bs.size)

        assertFailsWith<IllegalArgumentException> {
            bs.substring(-1, 8)
        }
        assertFailsWith<IllegalArgumentException> {
            bs.substring(0, 17)
        }
        assertFailsWith<IllegalArgumentException> {
            bs.substring(8, 4)
        }
    }

    @Test
    fun `test BitString substring returns empty`() {
        val bs = BitString("CAFE")
        val empty = bs.substring(8, 8)
        assertEquals(0, empty.size)
        assertEquals("", empty.toString())
    }

    @Test
    fun `test BitString compareTo`() {
        val bs1 = BitString("00")
        val bs2 = BitString("FF")
        val bs3 = BitString("CAFE")
        val bs4 = BitString("CAFEBABE")

        assertTrue(bs1 < bs2)
        assertTrue(bs2 > bs1)
        assertTrue(bs3 < bs4) // shorter comes before longer with same prefix
        assertEquals(0, bs1.compareTo(bs1))
    }

    @Test
    fun `test BitString compareTo with different sizes`() {
        val bs1 = BitString("F_") // 1111 (4 bits)
        val bs2 = BitString("FF") // 11111111 (8 bits)

        // Shorter with same prefix should come first
        assertTrue(bs1 < bs2)
    }

    @Test
    fun `test BitString toString`() {
        assertEquals("CAFE", BitString("CAFE").toString())
        assertEquals("8A_", BitString("8A_").toString())
        assertEquals("F", BitString("F").toString())
        assertEquals("", BitString("").toString())
    }

    @Test
    fun `test BitString toByteArray`() {
        val bs = BitString("CAFE")
        val bytes = bs.toByteArray()
        assertContentEquals(byteArrayOf(0xCA.toByte(), 0xFE.toByte()), bytes)
    }

    @Test
    fun `test BitString toByteArray with incomplete bytes`() {
        val bs = BitString("8A_") // 100010 (6 bits)
        val bytes = bs.toByteArray()
        assertEquals(1, bytes.size)
        assertEquals(0x8A.toByte(), bytes[0])
    }

    @Test
    fun `test empty BitString`() {
        val empty1 = BitString("")
        val empty2 = BitString("_")
        val empty3 = BitString("8_")
        val empty4 = BitString("0_")

        assertEquals(0, empty1.size)
        assertEquals(0, empty2.size)
        assertEquals(0, empty3.size)
        assertEquals(0, empty4.size)

        assertEquals("", empty1.toString())
        assertEquals("", empty2.toString())
        assertEquals("", empty3.toString())
        assertEquals("", empty4.toString())

        assertEquals(empty1, empty2)
        assertEquals(empty2, empty3)
        assertEquals(empty3, empty4)
    }

    @Test
    fun `test case insensitive hex parsing`() {
        val bs1 = BitString("cafe")
        val bs2 = BitString("CAFE")
        val bs3 = BitString("CaFe")

        assertEquals(bs1, bs2)
        assertEquals(bs2, bs3)
        assertEquals("CAFE", bs1.toString())
    }

    @Test
    fun `test various bit patterns`() {
        // All zeros
        val zeros = BitString("0000")
        assertEquals(16, zeros.size)
        for (i in 0 until zeros.size) {
            assertFalse(zeros[i], "Bit $i should be false")
        }

        // All ones
        val ones = BitString("FFFF")
        assertEquals(16, ones.size)
        for (i in 0 until ones.size) {
            assertTrue(ones[i], "Bit $i should be true")
        }

        // Alternating pattern
        val alt = BitString("AA") // 10101010
        assertEquals(true, alt[0])
        assertEquals(false, alt[1])
        assertEquals(true, alt[2])
        assertEquals(false, alt[3])
    }

    @Test
    fun `test BitString with maximum size`() {
        // Test with reasonably large bitstring (not full 1023 to keep test fast)
        val hexString = "A".repeat(64) // 256 bits
        val bs = BitString(hexString)
        assertEquals(256, bs.size)
    }

    @Test
    fun `test random BitStrings`() {
        repeat(50) {
            val length = Random.nextInt(1, 100)
            val bits = BooleanArray(length) { Random.nextBoolean() }

            // Convert to hex string
            val hexString = bitsToHexString(bits)
            val bs = BitString(hexString)

            assertEquals(length, bs.size, "Size mismatch for hex: $hexString")

            // Verify each bit
            for (i in bits.indices) {
                assertEquals(bits[i], bs[i], "Bit $i mismatch in hex: $hexString")
            }
        }
    }

    @Test
    fun `test BitString hashCode consistency`() {
        val bs = BitString("CAFE")
        val hash1 = bs.hashCode()
        val hash2 = bs.hashCode()
        assertEquals(hash1, hash2, "HashCode should be consistent")
    }

    @Test
    fun `test BitString backing array reference`() {
        val bytes = byteArrayOf(0xCA.toByte(), 0xFE.toByte(), 0x80.toByte())
        val bs = BitString(bytes)

        // Original bytes should not affect BitString
        bytes[0] = 0x00
        assertEquals(0xCA.toByte(), bs.toByteArray()[0])
    }

    @Test
    fun `test substring with full range returns same content`() {
        val bs = BitString("CAFEBABE")
        val sub = bs.substring(0, bs.size)
        assertEquals(bs.toString(), sub.toString())
        assertEquals(bs.size, sub.size)
    }

    @Test
    fun `test compareTo with empty strings`() {
        val empty = BitString("")
        val nonEmpty = BitString("FF")

        assertTrue(empty < nonEmpty)
        assertTrue(nonEmpty > empty)
        assertEquals(0, empty.compareTo(BitString("")))
    }

    // Comprehensive tests for bitsCompare function coverage
    @Test
    fun `test compare same reference`() {
        val bs = BitString("CAFEBABE")
        assertEquals(0, bs.compareTo(bs), "Same reference should be equal")
    }

    @Test
    fun `test compare byte-aligned bitstrings - 32-bit blocks`() {
        // Test fast path: both byte-aligned with >= 32 bits
        val bs1 = BitString("CAFEBABE")  // 32 bits, byte-aligned
        val bs2 = BitString("CAFEBABE")
        val bs3 = BitString("CAFEBABF")
        val bs4 = BitString("CAFEBAA0")

        assertEquals(0, bs1.compareTo(bs2), "Equal 32-bit aligned should be 0")
        assertTrue(bs1 < bs3, "CAFEBABE < CAFEBABF")
        assertTrue(bs4 < bs1, "CAFEBAA0 < CAFEBABE")
    }

    @Test
    fun `test compare byte-aligned bitstrings - large blocks`() {
        // Test with multiple 32-bit blocks (> 32 bits)
        val bs1 = BitString("CAFEBABECAFEBABE")  // 64 bits
        val bs2 = BitString("CAFEBABECAFEBABE")
        val bs3 = BitString("CAFEBABECAFEBABF")
        val bs4 = BitString("CAFEBABEDEADBEEF")

        assertEquals(0, bs1.compareTo(bs2), "Equal 64-bit aligned should be 0")
        assertTrue(bs1 < bs3, "Last bit difference")
        assertTrue(bs4 > bs1, "DEADBEEF > CAFEBABE in second block")
    }

    @Test
    fun `test compare byte-aligned with remainder bits`() {
        // Test byte-aligned with 1-31 remainder bits
        val bs1 = BitString("CAFE0")   // 20 bits (4 full bytes + 4 bits)
        val bs2 = BitString("CAFE0")
        val bs3 = BitString("CAFE8")   // Different in last nibble

        assertEquals(0, bs1.compareTo(bs2), "Equal with remainder")
        assertTrue(bs1 < bs3, "0 < 8 in remainder")
    }

    @Test
    fun `test compare equal non-zero bit offsets`() {
        // Test fast path: equal non-zero offsets
        // Create substrings with same bit offset
        val base1 = BitString("00CAFEBABE")  // 40 bits
        val base2 = BitString("00DEADBEEF")
        val base3 = BitString("00CAFEBABE")

        // Substring from bit 3 - both have offset 3
        val bs1 = base1.substring(3, 35)  // 32 bits starting at bit 3
        val bs2 = base2.substring(3, 35)
        val bs3 = base3.substring(3, 35)

        assertEquals(0, bs1.compareTo(bs3), "Equal with same offset")
        assertNotEquals(0, bs1.compareTo(bs2), "Different with same offset")
    }

    @Test
    fun `test compare equal offsets - large blocks`() {
        // Test equal offsets with >= 40 bits
        // base1 ends in ...BE (bits 68-71 = 1110), base2 ends in ...BF (bits 68-71 = 1111)
        // Need to include those bits in the substring to see the difference
        val base1 = BitString("00CAFEBABECAFEBABE")  // 72 bits
        val base2 = BitString("00CAFEBABECAFEBABF")  // Different last nibble

        val bs1 = base1.substring(5, 72)  // 67 bits starting at bit 5 (includes the difference)
        val bs2 = base2.substring(5, 72)  // 67 bits starting at bit 5

        // bs1 should be less than bs2 because the difference is now included
        assertTrue(bs1 < bs2, "Equal offsets, large blocks")
    }

    @Test
    fun `test compare different bit offsets`() {
        // Test general path: different bit offsets
        // CAFEBABE is 32 bits total
        val base1 = BitString("CAFEBABE")  // 32 bits
        val base2 = BitString("CAFEBABE")  // 32 bits

        val bs1 = base1.substring(2, 30)  // 28 bits
        val bs2 = base2.substring(2, 30)  // 28 bits same position

        assertEquals(0, bs1.compareTo(bs2), "Same offsets, equal content")
    }

    @Test
    fun `test compare different offsets - large blocks`() {
        // Test with large blocks (>= 40 bits)
        // Create identical long bitstrings and compare them
        val bs1 = BitString("CAFEBABECAFEBABE")   // 64 bits
        val bs2 = BitString("CAFEBABECAFEBABE")   // 64 bits

        assertEquals(0, bs1.compareTo(bs2), "Large equal content")

        val bs3 = BitString("CAFEBABECAFEBABF")   // Different at end
        assertTrue(bs1 < bs3, "Large blocks with difference")
    }

    @Test
    fun `test compare different offsets - differing content`() {
        val base1 = BitString("0CAFEBABE12345678")
        val base2 = BitString("00CAFEBABE87654321")

        val bs1 = base1.substring(1, 65)  // 64 bits at offset 1
        val bs2 = base2.substring(2, 66)  // 64 bits at offset 2

        assertNotEquals(0, bs1.compareTo(bs2), "Different offsets, different content")
    }

    @Test
    fun `test compare small bit counts`() {
        // Test with bitCount < 8
        val bs1 = BitString("8_")  // 1000 (4 bits)
        val bs2 = BitString("8_")
        val bs3 = BitString("C_")  // 1100 (4 bits)

        assertEquals(0, bs1.compareTo(bs2), "Equal small bitstrings")
        assertTrue(bs1 < bs3, "8 < C with small bits")
    }

    @Test
    fun `test compare zero length`() {
        val empty1 = BitString("")
        val empty2 = BitString("")

        assertEquals(0, empty1.compareTo(empty2), "Empty bitstrings should be equal")
    }

    @Test
    fun `test compare with length difference`() {
        // When common prefix is equal, shorter should come first
        val bs1 = BitString("CAFE")    // 16 bits
        val bs2 = BitString("CAFEB")   // 20 bits
        val bs3 = BitString("CAFEBA")  // 24 bits

        assertTrue(bs1 < bs2, "Shorter with same prefix comes first")
        assertTrue(bs2 < bs3, "Shorter with same prefix comes first")
    }

    @Test
    fun `test compare unsigned behavior`() {
        // Test that comparison is unsigned (0xFF > 0x7F)
        val bs1 = BitString("7F")  // 01111111
        val bs2 = BitString("FF")  // 11111111

        assertTrue(bs1 < bs2, "Unsigned: 7F < FF")

        val bs3 = BitString("80")  // 10000000
        val bs4 = BitString("7F")  // 01111111
        assertTrue(bs4 < bs3, "Unsigned: 7F < 80")
    }

    @Test
    fun `test compare with all paths - byte aligned remainder`() {
        // Specifically test remainder handling in byte-aligned path
        val bs1 = BitString("CAFE12")     // 24 bits
        val bs2 = BitString("CAFE12")
        val bs3 = BitString("CAFE13")

        assertEquals(0, bs1.compareTo(bs2))
        assertTrue(bs1 < bs3)
    }

    @Test
    fun `test compare edge case - 40 bits boundary`() {
        // Test boundary between different loop conditions (bits >= 40)
        // "CAFEBABE0" = 36 bits (9 hex digits * 4 bits)
        // "CAFEBABE00" = 40 bits (10 hex digits * 4 bits)
        val bs1 = BitString("CAFEBABE0")   // 36 bits
        val bs2 = BitString("CAFEBABE00")  // 40 bits
        val bs3 = BitString("CAFEBABE000") // 44 bits

        // Shorter with same prefix should come first
        assertTrue(bs1 < bs2, "36 bits < 40 bits with same prefix")
        assertTrue(bs2 < bs3, "40 bits < 44 bits with same prefix")
    }

    @Test
    fun `test compare with offset and small count`() {
        // Test small bit count with offsets
        val base1 = BitString("F0F0")
        val base2 = BitString("00F0")

        val bs1 = base1.substring(4, 12)  // 8 bits at offset 4
        val bs2 = base2.substring(4, 12)  // 8 bits at offset 4

        assertEquals(0, bs1.compareTo(bs2), "Small count with equal offsets")
    }

    @Test
    fun `test compare alternating patterns`() {
        // Test with alternating bit patterns
        val bs1 = BitString("AAAA")  // 10101010 repeated
        val bs2 = BitString("AAAA")
        val bs3 = BitString("5555")  // 01010101 repeated

        assertEquals(0, bs1.compareTo(bs2))
        assertTrue(bs3 < bs1, "0101... < 1010...")
    }

    @Test
    fun `test compare long aligned strings`() {
        // Test with strings longer than 64 bits to exercise multiple 32-bit block comparisons
        val hex1 = "A".repeat(32)  // 128 bits of 1010...
        val hex2 = "A".repeat(32)
        val hex3 = "A".repeat(31) + "B"  // Different in last byte

        val bs1 = BitString(hex1)
        val bs2 = BitString(hex2)
        val bs3 = BitString(hex3)

        assertEquals(0, bs1.compareTo(bs2), "Long equal aligned strings")
        assertTrue(bs3 > bs1, "Difference in last block")
    }

    @Test
    fun `test compare with varied offsets and lengths`() {
        // Complex scenario: different offsets, different lengths
        val base1 = BitString("123456789ABCDEF0")
        val base2 = BitString("0123456789ABCDEF")

        val bs1 = base1.substring(3, 35)   // 32 bits at offset 3
        val bs2 = base2.substring(5, 37)   // 32 bits at offset 5

        assertNotEquals(0, bs1.compareTo(bs2), "Different content, different offsets")
    }

    // Helper function to convert bits to hex string
    private fun bitsToHexString(bits: BooleanArray): String {
        if (bits.isEmpty()) return ""

        val sb = StringBuilder()
        var i = 0

        // Process full hex digits (4 bits each)
        while (i + 4 <= bits.size) {
            val nibble = (if (bits[i]) 8 else 0) +
                    (if (bits[i + 1]) 4 else 0) +
                    (if (bits[i + 2]) 2 else 0) +
                    (if (bits[i + 3]) 1 else 0)
            sb.append(nibble.toString(16).uppercase())
            i += 4
        }

        // Process remaining bits
        if (i < bits.size) {
            val remaining = bits.size - i
            var nibble = 0
            for (j in 0 until remaining) {
                if (bits[i + j]) {
                    nibble = nibble or (1 shl (3 - j))
                }
            }
            // Add completion: append 1 bit and pad to 4 bits
            nibble = nibble or (1 shl (3 - remaining))
            sb.append(nibble.toString(16).uppercase())
            sb.append('_')
        }

        return sb.toString()
    }

    private fun assertBitString(binary: String, hex: String) {
        try {
            val bs = BitString(hex)

            // Check size
            assertEquals(binary.length, bs.size, "Size mismatch for hex: $hex")

            // Check each bit
            for (i in binary.indices) {
                val expected = binary[i] == '1'
                assertEquals(expected, bs[i], "Bit $i mismatch for hex: $hex, binary: $binary")
            }

            // Check toString
            assertEquals(hex, bs.toString(), "toString mismatch for binary: $binary")

        } catch (e: Exception) {
            fail("Failed for binary: $binary, hex: $hex", e)
        }
    }
}

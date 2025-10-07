package org.ton.bitstring

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.fail

class BitStringTest {
    @Test
    fun `BitString creation`() {
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

    @Test
    fun `BitString concatenation without shifting`() {
        assertEquals(BitString("CAFEBABE"), BitString("CAFE") + BitString("BABE"))
        assertEquals(BitString("CAFEBABE"), BitString("CAFE") + byteArrayOf(0xBA.toByte(), 0xBE.toByte()))
        assertEquals(BitString("FEEDBEEF"), BitString("FEED") + BitString("BEEF"))
        assertEquals(BitString("FEEDBEEF"), BitString("FEED") + byteArrayOf(0xBE.toByte(), 0xEF.toByte()))

        val bytes1 = Random.nextBytes(8)
        val bytes2 = Random.nextBytes(8)
        val expectedBytes = bytes1 + bytes2
        assertEquals(BitString(expectedBytes), BitString(bytes1) + bytes2)
    }

    @Test
    fun `BitString concatenation with shifting`() {
        assertEquals(BitString.binary("100000001"), BitString.binary("10000000") + BitString.binary("1"))
        assertEquals(BitString.binary("1000000011"), BitString.binary("10000000") + BitString.binary("11"))
        assertEquals(BitString.binary("10000000111"), BitString.binary("10000000") + BitString.binary("111"))
        assertEquals(BitString.binary("100000001111"), BitString.binary("10000000") + BitString.binary("1111"))
        assertEquals(BitString.binary("1000000011111"), BitString.binary("10000000") + BitString.binary("11111"))
        assertEquals(BitString.binary("10000000111111"), BitString.binary("10000000") + BitString.binary("111111"))
        assertEquals(BitString.binary("100000001111111"), BitString.binary("10000000") + BitString.binary("1111111"))
    }

    @Test
    fun `BitString concatenation with double-shifting`() {
        assertEquals(BitString("AB"), BitString("A") + BitString("B"))
        assertEquals(BitString("BC"), BitString("B") + BitString("C"))
        assertEquals(BitString("CD"), BitString("C") + BitString("D"))
        assertEquals(BitString.binary("100000010000001"), BitString.binary("1000000") + BitString.binary("10000001"))
        assertEquals(BitString.binary("100000110000001"), BitString.binary("100000") + BitString.binary("110000001"))
        assertEquals(BitString.binary("100001110000001"), BitString.binary("10000") + BitString.binary("1110000001"))
        assertEquals(BitString.binary("100011110000001"), BitString.binary("1000") + BitString.binary("11110000001"))
        assertEquals(BitString.binary("100111110000001"), BitString.binary("100") + BitString.binary("111110000001"))
        assertEquals(BitString.binary("101111110000001"), BitString.binary("10") + BitString.binary("1111110000001"))
        assertEquals(BitString.binary("101111110000001"), BitString.binary("1") + BitString.binary("01111110000001"))
    }

    @Test
    fun `random BitString`() {
        repeat(100) {
            repeat(1023) { length ->
                val bits1 = BooleanArray(length) { Random.nextBoolean() }
                try {
                    val bitString1 = BitString(*bits1)

                    val bits2 = bitString1.toBooleanArray()
                    assertContentEquals(bits1, bits2)

                    val bytes1 = bitString1.toByteArray()
                    val bitString2 = BitString(bytes1, length)

                    assertEquals(bitString1, bitString2)
                    assertEquals(bitString1.toString(), bitString2.toString())
                    assertEquals(bitString1.toBooleanArray().joinToBits(), bitString2.toBooleanArray().joinToBits())
                    assertContentEquals(bitString1.toByteArray(), bitString2.toByteArray())
                } catch (e: Exception) {
                    fail("bits: ${bits1.joinToBits()}", e)
                }
            }
        }
    }

    @Test
    fun `toString on a zero number`() {
        assertEquals("0", BitString(List(4) { false }).toHexString())
        assertEquals("00000000", BitString(List(32) { false }).toHexString())
        assertEquals("0000000000000000", BitString(List(64) { false }).toHexString())
    }
}

fun assertBitString(binary: String, hex: String) {
    try {
        val binaryBits = BitString.binary(binary)
        val hexBits = BitString(hex)

        assertEquals(binaryBits, hexBits)
        assertEquals(hex, binaryBits.toHexString())
        assertEquals(hex, hexBits.toHexString())
        assertEquals(binary, binaryBits.toBooleanArray().joinToBits())
        assertEquals(binary, hexBits.toBooleanArray().joinToBits())
        assertEquals(binaryBits.toHexString(), hexBits.toHexString())
        assertContentEquals(binaryBits.toBooleanArray(), hexBits.toBooleanArray())
        assertContentEquals(binaryBits.toByteArray(), hexBits.toByteArray())
        assertEquals(BitString(binaryBits.size), BitString(hexBits.size))
        assertEquals(
            BitString(binaryBits.toByteArray(), binaryBits.size),
            BitString(hexBits.toByteArray(), hexBits.size)
        )
        assertEquals(BitString(binaryBits.toByteArray()), BitString(hexBits.toByteArray()))
        assertEquals(BitString(*binaryBits.toBooleanArray()), BitString(*hexBits.toBooleanArray()))
    } catch (e: Exception) {
        fail("binary: $binary hex: $hex", e)
    }
}

private fun BooleanArray.joinToBits() =
    joinToString(separator = "") { if (it) "1" else "0" }

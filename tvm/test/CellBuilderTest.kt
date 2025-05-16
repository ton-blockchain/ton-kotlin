package org.ton.kotlin.cell

import org.ton.kotlin.bigint.BigInt
import org.ton.kotlin.bitstring.BitString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class CellBuilderTest {
    @Test
    fun `build empty`() {
        val cell = CellBuilder.beginCell()
            .endCell()
        assertEquals(0, cell.bits.size)
        val empty = Cell()
        assertEquals(empty, cell)
    }

    @Test
    fun `build single bit`() {
        val cell = CellBuilder.beginCell()
            .storeBoolean(true)
            .endCell()
        assertEquals(Cell(BitString.of(true)), cell)
    }

    @Test
    fun `build multiple bits`() {
        val cell = CellBuilder.beginCell()
            .storeBoolean(true)
            .storeBoolean(false)
            .storeBoolean(false)
            .storeBoolean(true)
            .storeBoolean(false)
            .endCell()
        assertEquals(Cell(BitString.of(true, false, false, true, false)), cell)
    }

    @Test
    fun `fail on too many bits added`() {
        val builder = CellBuilder.beginCell()
            .storeUInt(0, 10) // fine for now
        assertEquals(10, builder.bits.size)
        builder.storeBits(*BooleanArray(1023 - builder.bits.size))
        println(builder.bits.size)
        assertFails {
            builder.storeBoolean(false)
        }
    }

    @Test
    fun `build a number`() {
        assertEquals(Cell("00"), CellBuilder.createCell { storeUInt(0, 8) })
        assertEquals(Cell("01"), CellBuilder.createCell { storeUInt(1, 8) })
        assertEquals(Cell("05"), CellBuilder.createCell { storeUInt(5, 8) })
        assertEquals(Cell("21"), CellBuilder.createCell { storeUInt(33, 8) })
        assertEquals(Cell("7F"), CellBuilder.createCell { storeUInt(127, 8) })
        assertEquals(Cell("80"), CellBuilder.createCell { storeUInt(128, 8) })
        assertEquals(Cell("FF"), CellBuilder.createCell { storeUInt(255, 8) })
        assertEquals(Cell("804_"), CellBuilder.createCell { storeUInt(256, 9) })

        assertFails { CellBuilder.createCell { storeUInt(256, 8) } }
        assertFails { CellBuilder.createCell { storeUInt(-1, 8) } }

        assertEquals(Cell("7F"), CellBuilder.createCell { storeInt(127, 8) })
        assertEquals(Cell("00"), CellBuilder.createCell { storeInt(0, 8) })
        assertEquals(Cell("FF"), CellBuilder.createCell { storeInt(-1, 8) })
        assertEquals(Cell("FB"), CellBuilder.createCell { storeInt(-5, 8) })
        assertEquals(Cell("DF"), CellBuilder.createCell { storeInt(-33, 8) })
        assertEquals(Cell("81"), CellBuilder.createCell { storeInt(-127, 8) })
        assertEquals(Cell("80"), CellBuilder.createCell { storeInt(-128, 8) })
        assertEquals(Cell("BFC_"), CellBuilder.createCell { storeInt(-129, 9) })
        assertEquals(Cell("4357"), CellBuilder.createCell { storeInt(17239, 16) })
        assertEquals(Cell("FDF_"), CellBuilder.createCell { storeInt(-17, 11) })
        assertEquals(Cell("3B9ACAEF"), CellBuilder.createCell { storeInt(1000000239, 32) })
        assertEquals(
            Cell("00000001BC16E45E4D41643_"),
            CellBuilder.createCell { storeInt(1000000239L * 1000000239, 91) }
        )
        assertEquals(
            Cell("989A386C05EFF862FFFFE23_"),
            CellBuilder.createCell { storeInt(BigInt("-1000000000000000000000000239"), 91) })

        assertFails { CellBuilder.createCell { storeUInt(-129, 8) } }
    }

    @Test
    fun `build multiple numbers`() {
        val cell = CellBuilder.beginCell()
            .storeUInt(0, 16)
            .storeUInt(1, 8)
            .storeUInt(1, 8)
            .storeUInt(69, 64)
            .endCell()
        assertEquals("000001010000000000000045", cell.bits.toByteArray().toHexString())
    }
}

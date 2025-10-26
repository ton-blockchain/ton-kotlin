package org.ton.cell

import org.ton.bigint.toInt
import kotlin.test.Test
import kotlin.test.assertEquals

class CellSliceTest {
    @Test
    fun `extract ints from slice`() {
        Cell("0000010100000000000000457C_").parse {
            assertEquals(0, loadUInt(16).toInt())
            assertEquals(1, loadUInt(8).toInt())
            assertEquals(1, loadUInt(8).toInt())
            assertEquals(69, loadUInt(64).toInt())
            assertEquals(15, loadUInt(5).toInt())
        }
        Cell("0000FF880FFFFFFFFFFFFFFDDC_").parse {
            assertEquals(0, loadInt(16).toInt())
            assertEquals(-1, loadInt(8).toInt())
            assertEquals(-15, loadInt(5).toInt())
            assertEquals(1, loadInt(8).toInt())
            assertEquals(-69, loadInt(64).toInt())
        }
    }
}

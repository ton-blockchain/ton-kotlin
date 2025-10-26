@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.cell

import kotlin.test.Test
import kotlin.test.assertEquals

class BagOfCellsTest {
    @Test
    fun simpleBocFromBytes() {
        val boc = BagOfCells("b5ee9c72010102010011000118000001010000000000000045010000".hexToByteArray())
        assertEquals(1, boc.rootCount)

        val loadedCell = boc.loadCell(boc.getRootCell())

        assertEquals(
            "000001010000000000000045",
            loadedCell.bits.toByteArray().toHexString()
        )
    }
}

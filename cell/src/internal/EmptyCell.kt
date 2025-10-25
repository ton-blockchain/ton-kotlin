package org.ton.sdk.cell.internal

import org.ton.sdk.cell.Cell
import org.ton.sdk.cell.CellDescriptor
import org.ton.sdk.cell.LoadedCell
import org.ton.sdk.crypto.HashBytes

internal object EmptyCell : LoadedCell {
    override val descriptor: CellDescriptor = CellDescriptor(0, 0)

    override fun virtualize(offset: Int): Cell = this

    override fun hash(level: Int): HashBytes = EMPTY_CELL_HASH

    override fun depth(level: Int): Int = 0

    override fun reference(index: Int): Cell? = null

    val EMPTY_CELL_HASH = HashBytes(
        ubyteArrayOf(
            // @formatter:off
            0x96u, 0xa2u, 0x96u, 0xd2u, 0x24u, 0xf2u, 0x85u, 0xc6u, 0x7bu, 0xeeu, 0x93u, 0xc3u, 0x0fu, 0x8au, 0x30u, 0x91u,
            0x57u, 0xf0u, 0xdau, 0xa3u, 0x5du, 0xc5u, 0xb8u, 0x7eu, 0x41u, 0x0bu, 0x78u, 0x63u, 0x0au, 0x09u, 0xcfu, 0xc7u,
            // @formatter:on
        )
    )
}

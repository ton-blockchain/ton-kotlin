@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.account

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.cell.CellContext
import org.ton.tlb.TlbCodec

/**
 * Special transactions execution flags.
 *
 * @see [StateInit]
 */
public data class SpecialFlags(
    /**
     * Account will be called at the beginning of each block.
     */
    val tick: Boolean,

    /**
     * Account will be called at the end of each block.
     */
    val tock: Boolean
) {
    public companion object : TlbCodec<SpecialFlags> by SpecialFlagsTlbCodec
}

private object SpecialFlagsTlbCodec : TlbCodec<SpecialFlags> {
    override fun storeTlb(
        builder: CellBuilder, value: SpecialFlags, context: CellContext
    ) {
        builder.storeUInt((if (value.tick) 0b10 else 0b00) or (if (value.tock) 0b01 else 0b00), 2)
    }

    override fun loadTlb(
        slice: CellSlice,
        context: CellContext
    ): SpecialFlags {
        val value = slice.loadUInt(2).toInt()
        val tick = value and 0b10 != 0
        val tock = value and 0b01 != 0
        return SpecialFlags(tick, tock)
    }
}

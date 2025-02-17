@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.message.address

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSizeable
import org.ton.tlb.TlbCodec


public sealed interface MsgAddress : CellSizeable {
    public companion object : TlbCodec<MsgAddress?> {
        override fun loadTlb(slice: CellSlice, context: CellContext): MsgAddress? {
            return if (slice.preloadBoolean()) {
                MsgAddressInt.loadTlb(slice, context)
            } else {
                AddrExtern.loadTlb(slice, context)
            }
        }

        override fun storeTlb(builder: CellBuilder, value: MsgAddress?, context: CellContext) {
            when (value) {
                is MsgAddressInt -> MsgAddressInt.storeTlb(builder, value, context)
                is AddrExtern -> AddrExtern.storeTlb(builder, value, context)
                null -> AddrExtern.storeTlb(builder, value, context)
            }
        }
    }
}

package org.ton.kotlin.account

import org.ton.kotlin.cell.Cell
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbCodec

/**
 * Simple TVM library.
 *
 * @see [StateInit]
 */
public data class SimpleLib(
    /**
     * Whether this library is accessible from other accounts.
     */
    val public: Boolean,

    /**
     * Library code.
     */
    val root: Cell
) {
    public companion object : TlbCodec<SimpleLib> by SimpleLibTlbCodec
}

private object SimpleLibTlbCodec : TlbCodec<SimpleLib> {
    override fun storeTlb(
        builder: CellBuilder, value: SimpleLib, context: CellContext
    ) {
        builder.storeBoolean(value.public)
        builder.storeRef(value.root)
    }

    override fun loadTlb(
        slice: CellSlice,
        context: CellContext
    ): SimpleLib {
        val public = slice.loadBoolean()
        val root = slice.loadRef()
        return SimpleLib(public, root)
    }
}

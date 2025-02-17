@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.message.address

import org.ton.bitstring.BitString
import org.ton.bitstring.ByteBackedMutableBitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSize
import org.ton.kotlin.cell.CellSizeable
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider

public data class Anycast(
    val rewritePrefix: BitString
) : CellSizeable {
    init {
        require(rewritePrefix.size in 1..30) { "required rewritePrefix size is 1..30, actual: ${rewritePrefix.size}" }
    }

    override val cellSize: CellSize get() = CellSize(5 + rewritePrefix.size, 0)

    public fun rewrite(address: BitString): BitString {
        val result = ByteBackedMutableBitString(address.size)
        rewritePrefix.copyInto(result)
        address.copyInto(result, rewritePrefix.size)
        return result
    }

    public companion object : TlbConstructorProvider<Anycast> by AnycastTlbConstructor
}

private object AnycastTlbConstructor : TlbConstructor<Anycast>(
    schema = "anycast_info\$_ depth:(#<= 30) { depth >= 1 } rewrite_pfx:(bits depth) = Anycast;"
) {
    override fun storeTlb(
        builder: CellBuilder, value: Anycast, context: CellContext
    ) {
        builder.storeUIntLeq(value.rewritePrefix.size, 30)
        builder.storeBitString(value.rewritePrefix)
    }

    override fun loadTlb(
        slice: CellSlice,
        context: CellContext
    ): Anycast {
        val depth = slice.loadUIntLeq(30).toInt()
        val rewritePfx = slice.loadBitString(depth)
        return Anycast(rewritePfx)
    }
}

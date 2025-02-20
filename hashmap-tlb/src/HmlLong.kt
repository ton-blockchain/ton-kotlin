@file:Suppress("DEPRECATION")

package org.ton.hashmap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.bitstring.BitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbNegatedConstructor
import org.ton.tlb.TlbNegatedResult
import org.ton.tlb.TlbPrettyPrinter

@Serializable
@SerialName("hml_long")
@Deprecated("Scheduled for removal")
public data class HmlLong(
    val n: Int,
    val s: BitString
) : HmLabel {
    public constructor(s: BitString) : this(s.size, s)

    override fun toBitString(): BitString = s

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("hml_long") {
        field("n", n)
        field("s", s)
    }

    override fun toString(): String = print().toString()

    public companion object {
        public fun tlbCodec(m: Int): TlbNegatedConstructor<HmlLong> = HashMapLabelLongTlbConstructor(m)
    }
}

private class HashMapLabelLongTlbConstructor(
    val m: Int
) : TlbNegatedConstructor<HmlLong>(
    schema = "hml_long\$10 {m:#} n:(#<= m) s:(n * Bit) = HmLabel ~n m;",
    id = ID
) {
    override fun storeNegatedTlb(
        cellBuilder: CellBuilder,
        value: HmlLong
    ): Int {
        cellBuilder.storeUIntLeq(value.n, m)
        cellBuilder.storeBitString(value.s)
        return value.n
    }

    override fun loadNegatedTlb(
        cellSlice: CellSlice
    ): TlbNegatedResult<HmlLong> {
        val n = cellSlice.loadUIntLeq(m).toInt()
        val s = cellSlice.loadBitString(n)
        return TlbNegatedResult(n, HmlLong(n, s))
    }

    companion object {
        val ID = BitString(true, false)
    }
}

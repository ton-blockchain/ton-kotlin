package org.ton.kotlin.hashmap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.*
import kotlin.jvm.JvmStatic

@Serializable
@SerialName("hml_short")
public data class HmlShort(
    val len: Unary,
    val s: BitString
) : HmLabel {
    public constructor(s: BitString) : this(Unary(s.size), s)

    override fun toBitString(): BitString = s

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer.type("hml_short") {
            field("len", len)
            field("s", s)
        }
    }

    override fun toString(): String = print().toString()

    public companion object {
        private val EMPTY = HmlShort(BitString.empty())

        @JvmStatic
        public fun empty(): HmlShort = EMPTY

        @JvmStatic
        public fun tlbCodec(): TlbNegatedConstructor<HmlShort> = HashMapLabelShortTlbConstructor
    }
}

private object HashMapLabelShortTlbConstructor : TlbNegatedConstructor<HmlShort>(
    schema = "hml_short\$0 {m:#} {n:#} len:(Unary ~n) s:(n * Bit) = HmLabel ~n m;",
    id = BitString(false)
) {
    override fun storeNegatedTlb(
        cellBuilder: CellBuilder,
        value: HmlShort
    ): Int {
        val n = cellBuilder.storeNegatedTlb(Unary, value.len)
        cellBuilder.storeBitString(value.s)
        return n
    }

    override fun loadNegatedTlb(
        cellSlice: CellSlice
    ): TlbNegatedResult<HmlShort> {
        val (n, len) = cellSlice.loadNegatedTlb(Unary)
        val s = cellSlice.loadBitString(n)
        return TlbNegatedResult(n, HmlShort(len, s))
    }
}

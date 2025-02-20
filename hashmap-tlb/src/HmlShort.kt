package org.ton.hashmap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.bitstring.BitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.*
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
        builder: CellBuilder,
        value: HmlShort
    ): Int {
        val n = builder.storeNegatedTlb(Unary, value.len)
        builder.storeBitString(value.s)
        return n
    }

    override fun loadNegatedTlb(
        slice: CellSlice
    ): TlbNegatedResult<HmlShort> {
        val (n, len) = slice.loadNegatedTlb(Unary)
        val s = slice.loadBitString(n)
        return TlbNegatedResult(n, HmlShort(len, s))
    }
}

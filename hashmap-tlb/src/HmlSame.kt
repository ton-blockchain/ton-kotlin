package org.ton.hashmap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.bigint.toInt
import org.ton.bitstring.BitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbNegatedConstructor
import org.ton.tlb.TlbNegatedResult
import org.ton.tlb.TlbPrettyPrinter
import kotlin.jvm.JvmStatic

@Serializable
@SerialName("hml_same")
public data class HmlSame(
    val v: Boolean,
    val n: Int
) : HmLabel {
    public constructor(v: Int, n: Int) : this(v != 0, n)

    override fun toBitString(): BitString = BitString(*BooleanArray(n) { v })

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("hml_same") {
        field("v", v)
        field("n", n)
    }

    override fun toString(): String = print().toString()

    public companion object {
        @JvmStatic
        public fun of(key: BitString, length: Int = key.size): HmlSame? {
            var zeroBitFound = false
            var oneBitFound = false
            key.forEach { bit ->
                if (bit) {
                    if (zeroBitFound) return null
                    else oneBitFound = true
                } else {
                    if (oneBitFound) return null
                    else zeroBitFound = true
                }
            }
            return HmlSame(!zeroBitFound, length)
        }

        public fun tlbCodec(m: Int): TlbNegatedConstructor<HmlSame> =
            HashMapLabelSameTlbConstructor(m)
    }
}

private class HashMapLabelSameTlbConstructor(
    val m: Int,
) : TlbNegatedConstructor<HmlSame>(
    schema = "hml_same\$11 {m:#} v:Bit n:(#<= m) = HmLabel ~n m;",
    id = ID
) {
    override fun storeNegatedTlb(
        cellBuilder: CellBuilder,
        value: HmlSame
    ): Int {
        cellBuilder.storeBit(value.v)
        cellBuilder.storeUIntLeq(value.n, m)
        return value.n
    }

    override fun loadNegatedTlb(
        cellSlice: CellSlice
    ): TlbNegatedResult<HmlSame> {
        val v = cellSlice.loadBit()
        val n = cellSlice.loadUIntLeq(m).toInt()
        return TlbNegatedResult(n, HmlSame(v, n))
    }

    companion object {
        val ID = BitString(true, true)
    }
}

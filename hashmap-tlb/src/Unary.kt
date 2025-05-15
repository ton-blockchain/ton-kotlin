@file:Suppress("OPT_IN_USAGE", "NOTHING_TO_INLINE")

package org.ton.kotlin.hashmap

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.*
import kotlin.jvm.JvmStatic

public inline fun Unary(depth: Int): Unary = Unary.of(depth)

@Serializable
@JsonClassDiscriminator("@type")
public sealed class Unary : TlbObject {
    public companion object : TlbNegatedCodec<Unary> by UnaryTlbCombinator {
        @JvmStatic
        public fun of(depth: Int): Unary {
            var unary: Unary = UnaryZero
            repeat(depth) {
                unary = UnarySuccess(unary)
            }
            return unary
        }

        @JvmStatic
        public fun tlbCodec(): TlbNegatedCodec<Unary> = UnaryTlbCombinator
    }
}

private object UnaryTlbCombinator : TlbNegatedCombinator<Unary>(
    Unary::class,
    UnaryZero::class to UnaryZeroTlbConstructor,
    UnarySuccess::class to UnarySuccessTlbConstructor,
)

private object UnarySuccessTlbConstructor : TlbNegatedConstructor<UnarySuccess>(
    schema = "unary_succ\$1 {n:#} x:(Unary ~n) = Unary ~(n + 1);"
) {
    override fun storeNegatedTlb(cellBuilder: CellBuilder, value: UnarySuccess): Int {
        return cellBuilder.storeNegatedTlb(Unary, value.x) + 1
    }

    override fun loadNegatedTlb(cellSlice: CellSlice): TlbNegatedResult<UnarySuccess> {
        val (n, x) = cellSlice.loadNegatedTlb(Unary)
        return TlbNegatedResult(n + 1, UnarySuccess(x))
    }
}

private object UnaryZeroTlbConstructor : TlbNegatedConstructor<UnaryZero>(
    schema = "unary_zero\$0 = Unary ~0;"
) {
    private val result = TlbNegatedResult(0, UnaryZero)
    override fun storeNegatedTlb(cellBuilder: CellBuilder, value: UnaryZero): Int = 0

    override fun loadNegatedTlb(cellSlice: CellSlice) = result
}

@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import kotlin.jvm.JvmStatic

@Suppress("NOTHING_TO_INLINE")
public inline fun <X> X?.toMaybe(): Maybe<X> = Maybe.of(this)

@JsonClassDiscriminator("@type")

public sealed interface Maybe<X> : TlbObject {
    public val value: X?

    public fun get(): X? = value

    public companion object {
        @JvmStatic
        public fun <X> of(value: X?): Maybe<X> = if (value != null) Just(value) else Nothing()

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        public fun <X> tlbCodec(x: TlbCodec<X>): TlbCodec<Maybe<X>> = MaybeTlbCombinator(x) as TlbCodec<Maybe<X>>

        public inline operator fun <X> invoke(x: TlbCodec<X>): TlbCodec<Maybe<X>> = tlbCodec(x)
    }
}

@SerialName("nothing")

public class Nothing<X> : Maybe<X> {
    override val value: X? = null
    override fun hashCode(): Int = 0
    override fun equals(other: Any?): Boolean = other is Nothing<*>
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("nothing")
    }

    override fun toString(): String = print().toString()
}

@SerialName("just")

public data class Just<X>(
    override val value: X
) : Maybe<X> {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("just") {
            field("value", value)
        }
    }

    override fun toString(): String = print().toString()
}

private class MaybeTlbCombinator(
    typeCodec: TlbCodec<*>,
    val justConstructor: JustConstructor<*> = JustConstructor(typeCodec)
) : TlbCombinator<Maybe<*>>(
    Maybe::class,
    Nothing::class to NothingConstructor,
    Just::class to justConstructor
)

private object NothingConstructor : TlbConstructor<Nothing<Any>>(
    schema = "nothing\$0 {X:Type} = Maybe X;",
    id = BitString(false)
) {
    private val nothing = Nothing<Any>()

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: Nothing<Any>
    ) {
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): Nothing<Any> = nothing
}

private class JustConstructor<X>(
    val typeCodec: TlbCodec<X>
) : TlbConstructor<Just<X>>(
    schema = "just\$1 {X:Type} value:X = Maybe X;",
    id = ID
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: Just<X>
    ) = cellBuilder {
        storeTlb(typeCodec, value.value)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): Just<X> = cellSlice {
        val value = cellSlice.loadTlb(typeCodec)
        Just(value)
    }

    companion object {
        val ID = BitString(true)
    }
}

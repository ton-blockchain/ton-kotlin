@file:Suppress("OPT_IN_USAGE")

package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.bitstring.BitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor
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

        @Suppress("NOTHING_TO_INLINE")
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
    justConstructor: JustConstructor<*> = JustConstructor(typeCodec)
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
        builder: CellBuilder,
        value: Nothing<Any>
    ) {
    }

    override fun loadTlb(
        slice: CellSlice
    ): Nothing<Any> = nothing
}

private class JustConstructor<X>(
    val typeCodec: TlbCodec<X>
) : TlbConstructor<Just<X>>(
    schema = "just\$1 {X:Type} value:X = Maybe X;",
    id = ID
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: Just<X>
    ) = builder {
        storeTlb(typeCodec, value.value)
    }

    override fun loadTlb(
        slice: CellSlice
    ): Just<X> = slice {
        val value = slice.loadTlb(typeCodec)
        Just(value)
    }

    companion object {
        val ID = BitString(true)
    }
}

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
public inline fun <X, Y> Pair<X?, Y?>.toEither(): Either<X, Y> = Either.of(first, second)

@JsonClassDiscriminator("@type")

public sealed interface Either<X, Y> : TlbObject {
    public val x: X?
    public val y: Y?

    public fun toPair(): Pair<X?, Y?> = x to y

    @SerialName("left")

    public class Left<X, Y>(
        public val value: X
    ) : Either<X, Y> {
        override val x: X? = value
        override val y: Y? = null

        public operator fun component1(): X? = x
        public operator fun component2(): Y? = y

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Left<*, *>) return false
            if (value != other.value) return false
            return true
        }

        override fun hashCode(): Int = value.hashCode()

        override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
            type("left") {
                field("value", value)
            }
        }

        override fun toString(): String = print().toString()
    }

    @SerialName("right")

    public class Right<X, Y>(
        public val value: Y
    ) : Either<X, Y> {
        override val x: X? = null
        override val y: Y? = value

        public operator fun component1(): X? = x
        public operator fun component2(): Y? = y

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Right<*, *>) return false
            if (value != other.value) return false
            return true
        }

        override fun hashCode(): Int = value.hashCode()

        override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
            type("right") {
                field("value", value)
            }
        }

        override fun toString(): String = print().toString()
    }

    public companion object {
        @JvmStatic
        public fun <X, Y> of(left: X?, right: Y?): Either<X, Y> {
            if (left != null) {
                return Left(left)
            }
            if (right != null) {
                return Right(right)
            }
            throw IllegalArgumentException("first & second == null; At least one element must be non-null")
        }

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        public fun <X, Y> tlbCodec(x: TlbCodec<X>, y: TlbCodec<Y>): TlbCodec<Either<X, Y>> =
            EitherTlbCombinator(x, y) as TlbCodec<Either<X, Y>>
    }
}

public operator fun <X, Y> Either.Companion.invoke(x: TlbCodec<X>, y: TlbCodec<Y>): TlbCodec<Either<X, Y>> =
    tlbCodec(x, y)

private class EitherTlbCombinator<X, Y>(
    x: TlbCodec<X>, y: TlbCodec<Y>,
    left: LeftTlbConstructor<X, Y> = LeftTlbConstructor(x),
    right: RightTlbConstructor<X, Y> = RightTlbConstructor(y)
) : TlbCombinator<Either<*, *>>(
    Either::class,
    Either.Left::class to left,
    Either.Right::class to right
)

private class LeftTlbConstructor<X, Y>(val x: TlbCodec<X>) : TlbConstructor<Either.Left<X, Y>>(
    schema = "left\$0 {X:Type} {Y:Type} value:X = Either X Y;",
    id = ID
) {
    override fun storeTlb(
        cellBuilder: CellBuilder, value: Either.Left<X, Y>
    ) = cellBuilder {
        storeTlb(x, value.value)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): Either.Left<X, Y> = cellSlice {
        val value = loadTlb(x)
        Either.Left(value)
    }

    companion object {
        val ID = BitString(false)
    }
}

private class RightTlbConstructor<X, Y>(val y: TlbCodec<Y>) : TlbConstructor<Either.Right<X, Y>>(
    schema = "right\$1 {X:Type} {Y:Type} value:Y = Either X Y;",
    id = ID
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: Either.Right<X, Y>
    ) = cellBuilder {
        storeTlb(y, value.value)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): Either.Right<X, Y> = cellSlice {
        val value = loadTlb(y)
        Either.Right(value)
    }

    companion object {
        val ID = BitString(true)
    }
}

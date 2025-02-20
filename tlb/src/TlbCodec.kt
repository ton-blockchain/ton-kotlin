@file:Suppress("NOTHING_TO_INLINE")

package org.ton.tlb

import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.parse
import org.ton.kotlin.cell.CellContext
import kotlin.jvm.JvmStatic

public interface TlbStorer<in T> {
    public fun storeTlb(builder: CellBuilder, value: T): Unit = storeTlb(builder, value, CellContext.EMPTY)
    public fun storeTlb(builder: CellBuilder, value: T, context: CellContext): Unit = storeTlb(builder, value)
    public fun createCell(value: T): Cell = CellBuilder.createCell {
        storeTlb(this, value, CellContext.EMPTY)
    }
}

@Deprecated("Scheduled to remove")
public interface TlbNegatedStorer<T> : TlbStorer<T> {
    public fun storeNegatedTlb(builder: CellBuilder, value: T): Int

    override fun storeTlb(builder: CellBuilder, value: T, context: CellContext) {
        storeNegatedTlb(builder, value)
    }
}

public interface TlbLoader<T> {
    public fun loadTlb(cell: Cell): T {
        val cellSlice = cell.beginParse()
        return loadTlb(cellSlice, CellContext.EMPTY)
    }

    public fun loadTlb(slice: CellSlice): T = loadTlb(slice, CellContext.EMPTY)

    public fun loadTlb(slice: CellSlice, context: CellContext): T = loadTlb(slice)
}

@Deprecated("Scheduled to remove")
public interface TlbNegatedLoader<T> : TlbLoader<T> {
    public fun loadNegatedTlb(cell: Cell): TlbNegatedResult<T> = cell.parse {
        loadNegatedTlb(this)
    }

    public fun loadNegatedTlb(slice: CellSlice): TlbNegatedResult<T>

    override fun loadTlb(slice: CellSlice): T = loadNegatedTlb(slice).value
}

public data class TlbNegatedResult<T>(
    val num: Int,
    val value: T
)

public interface TlbCodec<T> : TlbStorer<T>, TlbLoader<T> {
    public companion object {
        @JvmStatic
        public fun <X, Y> pair(first: TlbCodec<X>, second: TlbCodec<Y>): TlbCodec<Pair<X, Y>> =
            PairTlbCodec(first, second)

        @JvmStatic
        public fun long(bitCount: Int): TlbCodec<Long> = LongTlbCodec(bitCount)
    }
}

public class PairTlbCodec<X, Y>(
    private val first: TlbCodec<X>,
    private val second: TlbCodec<Y>
) : TlbCodec<Pair<X, Y>> {
    override fun storeTlb(builder: CellBuilder, value: Pair<X, Y>, context: CellContext) {
        first.storeTlb(builder, value.first, context)
        second.storeTlb(builder, value.second, context)
    }

    override fun loadTlb(slice: CellSlice, context: CellContext): Pair<X, Y> {
        val firstValue = first.loadTlb(slice)
        val secondValue = second.loadTlb(slice)
        return Pair(firstValue, secondValue)
    }
}

public class LongTlbCodec(
    private val bitCount: Int = Long.SIZE_BITS,
) : TlbCodec<Long> {
    override fun storeTlb(builder: CellBuilder, value: Long, context: CellContext) {
        builder.storeLong(value, bitCount)
    }

    override fun loadTlb(slice: CellSlice, context: CellContext): Long {
        return slice.loadLong(bitCount)
    }
}


@Suppress("DEPRECATION")
@Deprecated("Scheduled to remove")
public interface TlbNegatedCodec<T> : TlbCodec<T>, TlbNegatedStorer<T>, TlbNegatedLoader<T>

public fun <T> TlbCodec<T>.asNullable(): TlbCodec<T?> = NullableTlbCodec(this)

public class NullableTlbCodec<T>(
    private val codec: TlbCodec<T>
) : TlbCodec<T?> {
    override fun storeTlb(builder: CellBuilder, value: T?, context: CellContext) {
        if (value == null) {
            builder.storeBoolean(false)
        } else {
            builder.storeBoolean(true)
            codec.storeTlb(builder, value, context)
        }
    }

    override fun loadTlb(slice: CellSlice, context: CellContext): T? {
        return if (slice.loadBoolean()) {
            codec.loadTlb(slice, context)
        } else {
            null
        }
    }
}

public inline fun <T> CellSlice.loadTlb(codec: TlbLoader<T>): T {
    return codec.loadTlb(this)
}

@Suppress("DEPRECATION")
@Deprecated("Scheduled to remove")
public inline fun <T> CellSlice.loadNegatedTlb(codec: TlbNegatedLoader<T>): TlbNegatedResult<T> {
    return codec.loadNegatedTlb(this)
}

public inline fun <T> CellBuilder.storeTlb(
    codec: TlbStorer<T>,
    value: T,
    context: CellContext = CellContext.EMPTY
): CellBuilder = apply {
    codec.storeTlb(this, value, context)
}

public inline fun <T> CellBuilder.storeNegatedTlb(codec: TlbNegatedStorer<T>, value: T): Int =
    codec.storeNegatedTlb(this, value)

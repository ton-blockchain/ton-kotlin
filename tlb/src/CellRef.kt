package org.ton.tlb

import org.ton.bitstring.BitString
import org.ton.cell.*
import org.ton.kotlin.cell.CellContext
import kotlin.jvm.JvmStatic

public inline fun <T> CellRef(cell: Cell, codec: TlbCodec<T>): CellRef<T> = CellRef.valueOf(cell, codec)

public inline fun <T> CellRef(value: T, codec: TlbCodec<T>): CellRef<T> = CellRef.valueOf(value, codec)
public inline fun <T> CellRef(codec: TlbCodec<T>): TlbCodec<CellRef<T>> = CellRef.tlbCodec(codec)

public inline fun <T> Cell.asRef(codec: TlbCodec<T>): CellRef<T> = CellRef.valueOf(this, codec)

public interface CellRef<out T> : TlbObject {
    @Deprecated("use load() instead.", ReplaceWith("load()"))
    public val value: T get() = load()
    public val cell: Cell

    public fun load(): T = load(CellContext.EMPTY)
    public fun load(context: CellContext): T

    @Deprecated("use cell instead.", ReplaceWith("cell"))
    public fun toCell(codec: TlbCodec<@UnsafeVariance T>? = null): Cell = cell

    public fun hash(): BitString = hash(null)
    public fun hash(codec: TlbCodec<@UnsafeVariance T>?): BitString = toCell().hash()

    public operator fun getValue(thisRef: Any?, property: Any?): T = value

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        val value = value
        return if (value is TlbObject) {
            value.print(printer)
        } else {
            printer {
                type(value)
            }
        }
    }

    public companion object {
        @JvmStatic
        public fun <T> valueOf(cell: Cell, codec: TlbCodec<T>): CellRef<T> = CellRefImpl(cell, codec)

        @Suppress("DEPRECATION")
        @JvmStatic
        @Deprecated("Scheduled to be removed")
        public fun <T> valueOf(value: T): CellRef<T> = CellRefValue(value, null)

        @JvmStatic
        public fun <T> valueOf(value: T, codec: TlbCodec<T>): CellRef<T> = CellRefImpl(buildCell {
            codec.storeTlb(this, value, CellContext.EMPTY)
        }, codec)

        @JvmStatic
        public fun <T> tlbCodec(codec: TlbCodec<T>): TlbCodec<CellRef<T>> = CellRefTlbConstructor(codec)
    }
}

private class CellRefImpl<T>(
    override val cell: Cell,
    val codec: TlbCodec<T>
) : CellRef<T> {
    override fun load(context: CellContext): T {
        return context.loadCell(cell).parse {
            codec.loadTlb(this, context)
        }
    }

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return if (cell.type == CellType.PRUNED_BRANCH) {
            printer.type("!pruned_branch") {
                field("cell", cell.bits)
            }
        } else {
            super.print(printer)
        }
    }

    override fun toString(): String = "CellRef(${cell.bits}, hash=${cell.hash()})"
}

@Deprecated("Deprecated")
private class CellRefValue<T>(
    override val value: T,
    val codec: TlbCodec<T>? = null
) : CellRef<T> {
    override val cell: Cell
        get() {
            val currentCodec = codec ?: this.codec
            require(currentCodec != null) { "Codec is not specified" }
            return CellBuilder.createCell {
                currentCodec.storeTlb(this, value, CellContext.EMPTY)
            }
        }

    override fun load(context: CellContext): T {
        return value
    }

    override fun toString(): String = "CellRef($value)"
}

private class CellRefTlbConstructor<T>(
    val codec: TlbCodec<T>
) : TlbCodec<CellRef<T>> {
    override fun storeTlb(cellBuilder: CellBuilder, value: CellRef<T>) {
        cellBuilder.storeRef(value.toCell(codec))
    }

    override fun loadTlb(cellSlice: CellSlice): CellRef<T> {
        return cellSlice.loadRef().asRef(codec)
    }
}

public inline fun <T> CellBuilder.storeRef(codec: TlbCodec<T>, value: CellRef<T>) {
    storeRef(value.cell)
}

public inline fun <T> CellSlice.loadRef(codec: TlbCodec<T>): CellRef<T> {
    return loadRef().asRef(codec)
}

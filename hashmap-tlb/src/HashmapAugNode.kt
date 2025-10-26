package org.ton.hashmap

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.cell.CellContext
import org.ton.tlb.*
import kotlin.jvm.JvmStatic

public interface HashmapAugNode<X, Y> : AugmentedDictionary.Node<X, Y>, TlbObject {

    public val n: Int

    /**
     * ```tl-b
     * ahmn_leaf#_ {X:Type} {Y:Type} extra:Y value:X = HashmapAugNode 0 X Y;
     */
    public interface AhmnLeaf<X, Y> : HashmapAugNode<X, Y>, AugmentedDictionary.Leaf<X, Y> {
        override val n: Int get() = 0

        public override val extra: Y
        public override val value: X

        override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
            type("ahmn_leaf") {
                field("extra", extra)
                field("value", value)
            }
        }

        public companion object {
            @JvmStatic
            public fun <X, Y> tlbCodec(x: TlbCodec<X>, y: TlbCodec<Y>): TlbCodec<AhmnLeaf<X, Y>> =
                AhmnLeafTlbConstructor(x, y)
        }
    }

    /**
     * ```tl-b
     * ahmn_fork#_ {n:#} {X:Type} {Y:Type} left:^(HashmapAug n X Y)
     *   right:^(HashmapAug n X Y) extra:Y = HashmapAugNode (n + 1) X Y;
     */
    public interface AhmnFork<X, Y> : HashmapAugNode<X, Y> {
        public override val n: Int

        public val left: CellRef<HashmapAug<X, Y>>
        public val right: CellRef<HashmapAug<X, Y>>
        public override val extra: Y

        override val value: X? get() = null

        public fun loadLeft(): HashmapAug<X, Y> = left.load()
        public fun loadRight(): HashmapAug<X, Y> = right.load()

        override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
            type("ahmn_fork") {
                field("left", left)
                field("right", right)
                field("extra", extra)
            }
        }

        public companion object {
            @JvmStatic
            public fun <X, Y> tlbCodec(x: TlbCodec<X>, y: TlbCodec<Y>, n: Int): TlbCodec<AhmnFork<X, Y>> =
                AhmnForkTlbConstructor(x, y, n)
        }
    }

    public companion object {
        @JvmStatic
        public fun <X, Y> leaf(extra: Y, value: X): AhmnLeaf<X, Y> =
            AhmnLeafImpl(extra, value)

        @JvmStatic
        public fun <X, Y> fork(
            n: Int,
            left: CellRef<HashmapAug<X, Y>>,
            right: CellRef<HashmapAug<X, Y>>,
            extra: Y
        ): AhmnFork<X, Y> = AhmnForkImpl(n - 1, left, right, extra)

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        public fun <X, Y> tlbCodec(x: TlbCodec<X>, y: TlbCodec<Y>, n: Int): TlbCodec<HashmapAugNode<X, Y>> {
            return if (n == 0) {
                AhmnLeaf.tlbCodec(x, y)
            } else {
                AhmnFork.tlbCodec(x, y, n)
            } as TlbCodec<HashmapAugNode<X, Y>>
        }
    }
}

private data class AhmnLeafImpl<X, Y>(
    override val extra: Y,
    override val value: X,
) : HashmapAugNode.AhmnLeaf<X, Y> {
    override fun toString(): String = print().toString()
}

private data class AhmnForkImpl<X, Y>(
    override val n: Int,
    override val left: CellRef<HashmapAug<X, Y>>,
    override val right: CellRef<HashmapAug<X, Y>>,
    override val extra: Y,
) : HashmapAugNode.AhmnFork<X, Y> {
    override fun toString(): String = print().toString()
}

private class AhmnLeafTlbConstructor<X, Y>(
    val x: TlbCodec<X>,
    val y: TlbCodec<Y>,
) : TlbConstructor<HashmapAugNode.AhmnLeaf<X, Y>>(
    schema = "ahmn_leaf#_ {X:Type} {Y:Type} extra:Y value:X = HashmapAugNode 0 X Y"
) {
    override fun loadTlb(slice: CellSlice, context: CellContext): HashmapAugNode.AhmnLeaf<X, Y> {
        val extra = y.loadTlb(slice, context)
        val value = x.loadTlb(slice, context)
        return AhmnLeafImpl(extra, value)
    }

    override fun storeTlb(builder: CellBuilder, value: HashmapAugNode.AhmnLeaf<X, Y>, context: CellContext) {
        y.storeTlb(builder, value.extra, context)
        x.storeTlb(builder, value.value, context)
    }
}

private class AhmnForkTlbConstructor<X, Y>(
    x: TlbCodec<X>,
    val y: TlbCodec<Y>,
    val n: Int
) : TlbConstructor<HashmapAugNode.AhmnFork<X, Y>>(
    schema = "ahmn_fork#_ {n:#} {X:Type} {Y:Type} left:^(HashmapAug n X Y) right:^(HashmapAug n X Y) extra:Y = HashmapAugNode (n + 1) X Y"
) {
    private val hashmapAug = HashmapAug.tlbCodec(n - 1, x, y)

    override fun loadTlb(slice: CellSlice): HashmapAugNode.AhmnFork<X, Y> {
        val left = slice.loadRef(hashmapAug)
        val right = slice.loadRef(hashmapAug)
        val extra = y.loadTlb(slice)
        return AhmnForkImpl(n, left, right, extra)
    }

    override fun storeTlb(builder: CellBuilder, value: HashmapAugNode.AhmnFork<X, Y>) {
        check(value.n == this.n) { "n mismatch, expected: $n, actual: ${value.n}" }
        builder.storeRef(hashmapAug, value.left)
        builder.storeRef(hashmapAug, value.right)
        builder.storeTlb(y, value.extra)
    }
}

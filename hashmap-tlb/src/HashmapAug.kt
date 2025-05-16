package org.ton.kotlin.hashmap

import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.*
import kotlin.jvm.JvmStatic

public interface HashmapAug<X, Y> : AugmentedDictionary<X, Y>, TlbObject {

    public val n: Int

    override fun iterator(): Iterator<Pair<BitString, HashmapAugNode<X, Y>>>

    override fun get(key: BitString): HashmapAugNode.AhmnLeaf<X, Y>?

    /**
     * ```tl-b
     * ahm_edge#_ {n:#} {X:Type} {Y:Type} {l:#} {m:#}
     *   label:(HmLabel ~l n) {n = (~m) + l}
     *   node:(HashmapAugNode m X Y) = HashmapAug n X Y;
     */
    public interface AhmEdge<X, Y> : HashmapAug<X, Y> {
        public override val n: Int

        public val label: HmLabel
        public val node: HashmapAugNode<X, Y>

        override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
            type("ahm_edge") {
                field("label", label)
                field("node", node)
            }
        }

        public companion object {
            public fun <X, Y> tlbCodec(n: Int, x: TlbCodec<X>, y: TlbCodec<Y>): TlbCodec<AhmEdge<X, Y>> =
                AhmEdgeTlbConstructor(n, x, y)
        }
    }

    public companion object {
        @JvmStatic
        public fun <X, Y> edge(n: Int, node: HashmapAugNode<X, Y>): AhmEdge<X, Y> =
            edge(n, HmLabel.empty(), node)

        @JvmStatic
        public fun <X, Y> edge(n: Int, label: HmLabel, node: HashmapAugNode<X, Y>): AhmEdge<X, Y> =
            AhmeEdgeImpl(n, label, node)

        @Suppress("UNCHECKED_CAST")
        public fun <X, Y> tlbCodec(n: Int, x: TlbCodec<X>, y: TlbCodec<Y>): TlbCodec<HashmapAug<X, Y>> =
            AhmEdge.tlbCodec(n, x, y) as TlbCodec<HashmapAug<X, Y>>
    }
}

private data class AhmeEdgeImpl<X, Y>(
    override val n: Int,
    override val label: HmLabel,
    override val node: HashmapAugNode<X, Y>,
) : HashmapAug.AhmEdge<X, Y> {
    override fun get(key: BitString): HashmapAugNode.AhmnLeaf<X, Y>? {
        var edge: HashmapAug.AhmEdge<X, Y> = this
        var k = key
        while (true) {
            val label = edge.label.toBitString()
            val commonPrefix = k.commonPrefixWith(label.toBitString())
            when (val node = edge.node) {
                is HashmapAugNode.AhmnLeaf -> {
                    if (commonPrefix.size != label.size) {
                        return null
                    }
                    return node
                }

                is HashmapAugNode.AhmnFork -> {
                    edge = if (k[commonPrefix.size]) {
                        node.loadRight()
                    } else {
                        node.loadLeft()
                    } as HashmapAug.AhmEdge<X, Y>
                    k = k.slice(commonPrefix.size + 1)
                }
            }
        }
    }

    override fun iterator(): Iterator<Pair<BitString, HashmapAugNode<X, Y>>> =
        AhmnNodeIterator(this)

    override fun toString(): String = print().toString()
}

internal class AhmnNodeIterator<X, Y>(
    start: HashmapAug.AhmEdge<X, Y>?
) : AbstractIterator<Pair<BitString, HashmapAugNode<X, Y>>>() {
    val state = ArrayDeque<WalkState<X, Y>>()

    init {
        if (start != null) {
            addState(start.label.toBitString(), start.node)
        } else {
            done()
        }
    }

    private fun addState(prefix: BitString, node: HashmapAugNode<X, Y>) {
        when (node) {
            is HashmapAugNode.AhmnFork<X, Y> -> state.addFirst(WalkState.Fork(prefix, node))
            is HashmapAugNode.AhmnLeaf<X, Y> -> state.addFirst(WalkState.Leaf(prefix, node))
        }
    }

    sealed class WalkState<X, Y>(
        open val node: HashmapAugNode<X, Y>
    ) {
        abstract fun step(): Pair<BitString, HashmapAugNode<X, Y>>?

        class Leaf<X, Y>(
            private val prefix: BitString,
            override val node: HashmapAugNode.AhmnLeaf<X, Y>
        ) : WalkState<X, Y>(node) {
            var visited = false

            override fun step(): Pair<BitString, HashmapAugNode<X, Y>>? {
                if (visited) return null
                visited = true
                return prefix to node
            }
        }

        class Fork<X, Y>(
            val prefix: BitString,
            override val node: HashmapAugNode.AhmnFork<X, Y>
        ) : WalkState<X, Y>(node) {
            private var rootVisited = false
            private var leftVisited = false
            private var rightVisited = false

            override fun step(): Pair<BitString, HashmapAugNode<X, Y>>? {
                return if (!rootVisited) {
                    rootVisited = true
                    return prefix to node
                } else if (leftVisited) {
                    if (rightVisited) null
                    else {
                        rightVisited = true
                        val edge = node.right.value as HashmapAug.AhmEdge
                        val newPrefix = CellBuilder().apply {
                            storeBitString(prefix)
                            storeBoolean(true)
                            storeBitString(edge.label.toBitString())
                        }.bits.toBitString()
                        newPrefix to edge.node
                    }
                } else {
                    leftVisited = true
                    val edge = node.left.value as HashmapAug.AhmEdge
                    val newPrefix = CellBuilder().apply {
                        storeBitString(prefix)
                        storeBoolean(false)
                        storeBitString(edge.label.toBitString())
                    }.bits.toBitString()
                    newPrefix to edge.node
                }
            }
        }
    }

    override fun computeNext() {
        val nextValue = gotoNext()
        if (nextValue != null) {
            setNext(nextValue)
        } else {
            done()
        }
    }

    private tailrec fun gotoNext(): Pair<BitString, HashmapAugNode<X, Y>>? {
        val topState = state.firstOrNull() ?: return null
        val edge = topState.step()
        return if (edge == null) {
            state.removeFirst()
            gotoNext()
        } else {
            val (prefix, node) = edge
            if (node == topState.node || node is HashmapAugNode.AhmnLeaf<X, Y>) {
                edge
            } else {
                addState(prefix, node)
                gotoNext()
            }
        }
    }
}

private class AhmEdgeTlbConstructor<X, Y>(
    val n: Int,
    val x: TlbCodec<X>,
    val y: TlbCodec<Y>,
) : TlbConstructor<HashmapAug.AhmEdge<X, Y>>(
    schema = "ahm_edge#_ {n:#} {X:Type} {Y:Type} {l:#} {m:#} label:(HmLabel ~l n) {n = (~m) + l} node:(HashmapAugNode m X Y) = HashmapAug n X Y"
) {
    override fun loadTlb(cellSlice: CellSlice): HashmapAug.AhmEdge<X, Y> {
        val (l, label) = cellSlice.loadNegatedTlb(HmLabel.tlbCodec(n))
        val m = n - l
        val node = cellSlice.loadTlb(HashmapAugNode.tlbCodec(x, y, m))
        return HashmapAug.edge(n, label, node)
    }

    override fun storeTlb(cellBuilder: CellBuilder, value: HashmapAug.AhmEdge<X, Y>) {
        check(value.n == n) { "Invalid n, expected: $n, actual: ${value.n}" }
        val l = cellBuilder.storeNegatedTlb(HmLabel.tlbCodec(n), value.label)
        val m = n - l
        cellBuilder.storeTlb(HashmapAugNode.tlbCodec(x, y, m), value.node)
    }
}

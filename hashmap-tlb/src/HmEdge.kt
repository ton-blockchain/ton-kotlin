package org.ton.hashmap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.bitstring.BitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.*
import kotlin.jvm.JvmStatic

@Serializable
@SerialName("hm_edge")
public data class HmEdge<T>(
    val label: HmLabel,
    val node: HashMapNode<T>
) : Iterable<Pair<BitString, T>>, TlbObject {
    override fun iterator(): Iterator<Pair<BitString, T>> = HmEdgeIterator(this)

    public fun set(key: BitString, value: T): HmEdge<T> {
        check(!key.isEmpty())
        val label = label.toBitString()
        if (label == key) {
            // replace existing leaf
            return HmEdge(this.label, HmnLeaf(value))
        } else if (label.isEmpty()) {
            // 1-bit edge
            node as HmnFork<T>
            return HmEdge(this.label, node.set(key, value))
        } else {
            val labelPrefix = label.commonPrefixWith(key)
            val labelReminder = label.slice(labelPrefix.size)
            val keyReminder = key.slice(labelPrefix.size)

            if (keyReminder.isEmpty()) {
                throw IllegalArgumentException("variable length key: $key")
            } else if (!labelReminder.isEmpty() && !keyReminder.isEmpty()) {
                // forking
                val (left, right) = if (keyReminder[0]) {
                    HmEdge(HmLabel(labelReminder.slice(1)), node) to
                            HmEdge(HmLabel(keyReminder.slice(1)), HmnLeaf(value))
                } else {
                    HmEdge(HmLabel(keyReminder.slice(1)), HmnLeaf(value)) to
                            HmEdge(HmLabel(labelReminder.slice(1)), node)
                }
                return HmEdge(HmLabel(labelPrefix), HmnFork(left, right))
            } else if (!labelPrefix.isEmpty() && labelReminder.isEmpty() && !keyReminder.isEmpty()) {
                // next iteration
                node as HmnFork<T>
                val newNode = node.set(keyReminder, value)
                return HmEdge(HmLabel(labelPrefix), newNode)
            }
            throw IllegalStateException()
        }
    }

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("hm_edge") {
            field("label", label)
            field("node", node)
        }
    }

    override fun toString(): String = print().toString()

    public companion object {
        public const val ADD: Int = 0x01
        public const val REPLACE: Int = 0x02

        @JvmStatic
        public fun <X> tlbCodec(n: Int, x: TlbCodec<X>): TlbCodec<HmEdge<X>> =
            HashMapEdgeTlbConstructor(n, x)
    }
}

private class HmEdgeIterator<T>(
    start: HmEdge<T>
) : AbstractIterator<Pair<BitString, T>>() {
    val state = ArrayDeque<WalkState<T>>()

    init {
        addState(start.label.toBitString(), start.node)
    }

    private fun addState(prefix: BitString, node: HashMapNode<T>) {
        when (node) {
            is HmnFork<T> -> state.addFirst(WalkState.Fork(prefix, node))
            is HmnLeaf<T> -> state.addFirst(WalkState.Leaf(prefix, node))
        }
    }

    sealed class WalkState<T>(open val node: HashMapNode<T>) {
        abstract fun step(): Pair<BitString, HashMapNode<T>>?

        class Leaf<T>(
            private val prefix: BitString,
            override val node: HmnLeaf<T>
        ) : WalkState<T>(node) {
            var visited = false

            override fun step(): Pair<BitString, HashMapNode<T>>? {
                if (visited) return null
                visited = true
                return prefix to node
            }
        }

        class Fork<T>(
            val prefix: BitString,
            override val node: HmnFork<T>
        ) : WalkState<T>(node) {
            private var leftVisited = false
            private var rightVisited = false

            override fun step(): Pair<BitString, HashMapNode<T>>? {
                return if (leftVisited) {
                    if (rightVisited) {
                        null
                    } else {
                        rightVisited = true
                        val newPrefix = CellBuilder().apply {
                            storeBits(prefix)
                            storeBit(true)
                            storeBits(node.right.value.label.toBitString())
                        }.bits.toBitString()
                        newPrefix to node.right.value.node
                    }
                } else {
                    leftVisited = true
                    val newPrefix = CellBuilder().apply {
                        storeBits(prefix)
                        storeBit(false)
                        storeBits(node.left.value.label.toBitString())
                    }.bits.toBitString()
                    newPrefix to node.left.value.node
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

    private tailrec fun gotoNext(): Pair<BitString, T>? {
        val topState = state.firstOrNull() ?: return null
        val edge = topState.step()
        return if (edge == null) {
            state.removeFirst()
            gotoNext()
        } else {
            val (prefix, node) = edge
            if (node is HmnLeaf<T>) {
                prefix to node.value
            } else {
                addState(prefix, node)
                gotoNext()
            }
        }
    }
}

private class HashMapEdgeTlbConstructor<X>(
    val n: Int,
    val x: TlbCodec<X>
) : TlbConstructor<HmEdge<X>>(
    schema = "hm_edge#_ {n:#} {X:Type} {l:#} {m:#} label:(HmLabel ~l n) {n = (~m) + l} node:(HashmapNode m X) = Hashmap n X;",
    id = BitString.empty()
) {
    private val hashMapLabelCodec = HmLabel.tlbCodec(n)

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: HmEdge<X>
    ) {
        val l = cellBuilder.storeNegatedTlb(hashMapLabelCodec, value.label)
        val m = n - l
        cellBuilder.storeTlb(HashMapNode.tlbCodec(m, x), value.node)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): HmEdge<X> {
        val (l, label) = cellSlice.loadNegatedTlb(hashMapLabelCodec)
        val m = n - l
        val node = cellSlice.loadTlb(HashMapNode.tlbCodec(m, x))
        return HmEdge(label, node)
    }
}

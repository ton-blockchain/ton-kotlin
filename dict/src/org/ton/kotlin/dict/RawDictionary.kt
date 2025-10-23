@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.dict

import org.ton.bitstring.BitString
import org.ton.bitstring.ByteBackedMutableBitString
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.DataCell
import org.ton.cell.exception.CellUnderflowException
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.dict.internal.SetMode

/**
 * Typed dictionary with fixed length keys.
 */
public class RawDictionary(
    root: Cell?,
    public val keySize: Int,
) : Iterable<Map.Entry<BitString, CellSlice>> {
    public var root: Cell? = root
        private set

    public constructor(
        keySize: Int,
    ) : this(null, keySize)

    public fun isEmpty(): Boolean = root == null

    public fun isNotEmpty(): Boolean = !isEmpty()

    public fun iterator(context: CellContext): Iterator<Map.Entry<BitString, CellSlice>> =
        RawDictIterator(root, keySize, context)

    override fun iterator(): Iterator<Map.Entry<BitString, CellSlice>> = iterator(CellContext.EMPTY)

    public operator fun set(key: BitString, value: CellSlice): CellSlice? = set(key, value, CellContext.EMPTY)

    public fun set(
        key: BitString,
        value: CellSlice,
        context: CellContext,
    ): CellSlice? {
        return dictSet(
            key,
            0,
            key.size,
            value,
            SetMode.Set,
            context,
        )
    }

    public operator fun get(key: BitString): CellSlice? = get(key, CellContext.EMPTY)

    public operator fun contains(key: BitString): Boolean = get(key) != null

    public fun get(
        key: BitString,
        context: CellContext,
    ): CellSlice? {
        require(key.size == keySize)
        var data = context.loadCell(root ?: return null).beginParse()
        var n = keySize
        var keyOffset = 0
        while (true) {
            val label = data.readLabel(n)
            if (!label.isEmpty() && commonPrefixLength(label, key, keyOffset) == 0) {
                return null
            }
            n = n - label.size
            if (n <= 0) {
                return data
            }
            keyOffset += label.size
            val bit = if (key[keyOffset++]) 1 else 0
            n--
            data = context.loadCell(data.preloadRef(bit)).beginParse()
        }
    }

    public fun clear() {
        this.root = null
    }

    // TODO: optimize
    public fun remove(key: BitString, context: CellContext = CellContext.EMPTY): CellSlice? {
        require(key.size == keySize)
        val newDict = RawDictionary(null, keySize)
        var found: CellSlice? = null
        iterator(context).forEach { (k, v) ->
            if (key != k) {
                newDict.set(k, v, context)
            } else {
                found = v
            }
        }
        root = newDict.root
        return found
    }

    private fun commonPrefixLength(
        label: BitString,
        key: BitString,
        keyOffset: Int = 0,
    ): Int {
        val shortestLength = minOf(key.size, label.size)
        var prefixLen = 0
        while (prefixLen < shortestLength && key[keyOffset + prefixLen] == label[prefixLen]) {
            prefixLen++
        }
        return prefixLen
    }

    private fun dictSet(
        key: BitString,
        startIndex: Int,
        endIndex: Int,
        value: CellSlice,
        mode: SetMode,
        context: CellContext
    ): CellSlice? {
        val root = root
        if (root == null) {
            if (mode == SetMode.Replace) {
                return null
            }
            val builder = CellBuilder()
            builder.storeLabel(keySize, key, startIndex, endIndex)
            builder.storeSlice(value)
            this.root = context.finalizeCell(builder)
            return null
        }
        var keyOffset: Int = startIndex
        var data = context.loadCell(root)
        val builder = CellBuilder()

        val stack = ArrayDeque<Segment>()
        var leaf: Cell
        var oldValue: CellSlice? = null
        while (true) {
            var keyLength = endIndex - keyOffset
            val remainingData = data.beginParse()

            val label = remainingData.readLabel(keyLength)

            val prefixLen = commonPrefixLength(label, key, keyOffset)

            if (prefixLen == keyLength) {
                if (mode == SetMode.Add) {
                    return stack.lastOrNull()?.data?.beginParse()
                }
                builder.reset()
                builder.storeLabel(keySize, key, startIndex, endIndex)
                builder.storeSlice(value)
                leaf = context.finalizeCell(builder)
                oldValue = remainingData
                break
            } else if (prefixLen < keyLength) {
                if (prefixLen < label.size) {
                    // have to insert a new node (fork) inside the current edge
                    if (mode == SetMode.Replace) {
                        return null
                    }
                    val prevKeyLength = keyLength
                    keyOffset += prefixLen + 1
                    keyLength = endIndex - keyOffset
                    val oldToRight = label[prefixLen]
//                    println("label: ${label.toBinary().substring(prefixLen + 1, label.size)}")
//                    println("key length: $keyLength")
//                    println("rem data: ${remainingData.data.toBinary()}")
                    builder.reset()
                    builder.storeLabel(keyLength, label, prefixLen + 1, label.size)
                    builder.storeSlice(remainingData)
                    val left = context.finalizeCell(builder)

                    builder.reset()
                    builder.storeLabel(keyLength, key, keyOffset, endIndex)
                    builder.storeSlice(value)
                    val right = context.finalizeCell(builder)
//                    println("tree left: ${left}")
//                    println("data left: ${left.bits.toBinary()}")
//                    println("leaf right: ${right.hash()}")
                    builder.reset()
                    builder.storeLabel(prevKeyLength, label, 0, prefixLen)
                    if (oldToRight) {
                        builder.storeRef(right)
                        builder.storeRef(left)
                    } else {
                        builder.storeRef(left)
                        builder.storeRef(right)
                    }

                    leaf = context.finalizeCell(builder)
                    break
                }

                if (data.refs.size != 2) {
                    throw CellUnderflowException("Not enough references in fork")
                }
                keyOffset += prefixLen
                val nextBranch = key[keyOffset++]
                val child = context.loadCell(data.refs[if (nextBranch) 1 else 0])
//                println("child: ${child.hash()}")
                stack.addLast(
                    Segment(
                        data = data,
                        isRightNext = nextBranch,
                        keyBitLength = keyLength
                    )
                )
                data = child
            } else {
                throw RuntimeException("LCP of prefix and key can't be greater than key")
            }
        }

//        println("rebuild by leaf: ${leaf.hash()}")
        // rebuild the tree starting from leaves
        while (stack.isNotEmpty()) {
            val last = stack.removeLast()
            val left: Cell
            val right: Cell
            if (last.isRightNext) {
                left = last.data.refs[0]
                right = leaf
            } else {
                left = leaf
                right = last.data.refs[1]
            }
            val builder = CellBuilder()
            builder.storeBitString(last.data.bits)
            builder.storeRef(left)
            builder.storeRef(right)
            leaf = context.finalizeCell(builder)
        }

        this.root = leaf
        return oldValue
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RawDictionary) return false
        return root == other.root
    }

    override fun hashCode(): Int = root.hashCode()

    override fun toString(): String = "RawDictionary(${root?.hash()})"

    private class Segment(
        val data: DataCell,
        /**
         * Which branch to take when traversing the tree,
         * `false` - left, `true` - right
         */
        val isRightNext: Boolean,
        val keyBitLength: Int,
    )

    public companion object {
        public fun loadFromSlice(
            slice: CellSlice,
            keySize: Int,
            context: CellContext = CellContext.EMPTY
        ): RawDictionary {
            val builder = CellBuilder()
            builder.storeSlice(slice)
            val cell = context.finalizeCell(builder)
            return RawDictionary(cell, keySize)
        }
    }
}

internal class RawDictEntry(
    override val key: BitString,
    override val value: CellSlice,
) : Map.Entry<BitString, CellSlice>

internal class RawDictIterator(
    private var root: Cell?,
    private val keyBitCount: Int,
    private val cellContext: CellContext
) : Iterator<Map.Entry<BitString, CellSlice>> {
    private val path = ArrayDeque<Fork>()
    private var order: Int = 0
    private val key = ByteBackedMutableBitString(keyBitCount)
    private var leaf: CellSlice? = null

    init {
        if (root != null) {
            rewind(false)
        }
    }

    override fun next(): Map.Entry<BitString, CellSlice> {
        val leaf = leaf ?: throw NoSuchElementException()
        val key = ByteBackedMutableBitString(keyBitCount).apply {
            key.copyInto(this)
        }
        nextLeaf(0)
        return RawDictEntry(key, leaf)
    }

    override fun hasNext(): Boolean = leaf != null

    private fun nextLeaf(goBack: Int): CellSlice? {
        if (root == null || leaf == null) {
            throw NoSuchElementException()
        }
        leaf = null
        val mode = order xor -goBack
        while (path.isNotEmpty()) {
            val pe = path.last()
            val bit = (mode ushr if (pe.pos > 0) 1 else 0) and 1
            if (pe.v == (bit != 0)) {
                pe.rotate()
                return divide(mode)
            }
            path.removeLast()
        }
        return null
    }

    private fun rewind(toEnd: Boolean): CellSlice {
        val mode = order xor if (toEnd) -1 else 0
        return divide(mode)
    }

    private fun divide(mode: Int): CellSlice {
        var mode = mode
        var n = keyBitCount
        var m = 0
        var node = if (path.isEmpty()) {
            root
        } else {
            val last = path.last()
            m = last.pos + 1
            n = n - m
            mode = mode ushr 1
            last.next
        } ?: throw NoSuchElementException()
        while (true) {
            val slice = cellContext.loadCell(node).beginParse()
            val label = slice.readLabel(n)
            label.copyInto(key, m)
            m += label.size
            n -= label.size
            if (n == 0) {
                leaf = slice
                return slice
            }
            if (!label.isEmpty()) {
                mode = mode ushr 1
            }
            val bit = mode and 1
            node = slice.preloadRef(bit)
            val alt = slice.preloadRef(1 - bit)
            val v = bit != 0
            path.add(Fork(node, alt, m, v))
            key[m++] = v
            n--
            mode = mode ushr 1
        }
    }

    private inner class Fork(
        var next: Cell? = null,
        var alt: Cell? = null,
        val pos: Int = -1,
        var v: Boolean = false
    ) {
        fun rotate() {
            val tmp = next
            next = alt
            alt = tmp
            v = !v
            key[pos] = v
        }
    }
}

package org.ton.kotlin.tvm

import org.ton.bigint.BigInt
import org.ton.bigint.sign
import org.ton.bigint.toBigInt
import org.ton.cell.Cell
import org.ton.cell.CellSlice
import org.ton.kotlin.tvm.exception.StackUnderflowException

private const val INCREMENT = 256

public fun stackOf(vararg elements: Any): Stack {
    val stack = Stack()
    for (element in elements) {
        when (element) {
            is BigInt -> stack.pushInt(element)
            is Number -> stack.pushInt(element.toLong())
            is Boolean -> stack.pushBoolean(element)
            is Cell -> stack.pushCell(element)
            is CellSlice -> stack.pushSlice(element)
            is TvmContinuation -> stack.pushContinuation(element)
            else -> throw IllegalArgumentException("Unsupported type ${element::class}")
        }
    }
    return stack
}

public class Stack {
    private var elements = arrayOfNulls<Any?>(INCREMENT)

    public var depth: Int = 0

    public fun pop(): Any {
        val currentDepth = depth
        if (currentDepth == 0) {
            throw StackUnderflowException()
        }
        val elements = elements
        val newDepth = currentDepth - 1
        val removed = elements[newDepth]
        elements[newDepth] = null
        depth = newDepth
        return removed as Any
    }

    public fun pushInt(value: Long) {
        pushElement(value.toBigInt())
    }

    public fun pushInt(value: BigInt) {
        pushElement(value)
    }

    public fun pushBoolean(value: Boolean) {
        pushElement(if (value) (-1).toBigInt() else 0.toBigInt())
    }

    public fun pushCell(value: Cell) {
        pushElement(value)
    }

    public fun pushSlice(value: CellSlice) {
        pushElement(value)
    }

    public fun pushContinuation(value: TvmContinuation) {
        pushElement(value)
    }

    public fun pushElement(operand: Any) {
        val currentDepth = depth
        var elements = elements
        val currentCapacity = elements.size
        val nextDepth = currentDepth + 1
        if (nextDepth > currentCapacity) {
            elements = elements.copyOf(currentCapacity + INCREMENT)
            this.elements = elements
        }
        elements[currentDepth] = operand
        depth = nextDepth
    }

    public fun pushCopy(offset: Int) {
        var elements = elements
        val currentDepth = depth
        val value = elements[currentDepth - 1 - offset]
        val nextDepth = currentDepth + 1
        val currentCapacity = elements.size
        if (nextDepth > currentCapacity) {
            elements = elements.copyOf(currentCapacity + INCREMENT)
            this.elements = elements
        }
        elements[currentDepth] = value
        depth = nextDepth
    }

    public fun reverse(fromOffset: Int, toOffset: Int) {
        val length = toOffset - fromOffset
        val top = depth - 1
        val fromIndex = top - fromOffset - 1
        val toIndex = top - toOffset
        val elements = elements
        for (i in 0..length / 2) {
            val firstIndex = fromIndex + i
            val secondIndex = toIndex - i
            val firstValue = elements[firstIndex]
            val secondValue = elements[secondIndex]
            elements[firstIndex] = secondValue
            elements[secondIndex] = firstValue
        }
    }

    public fun dropTop(count: Int) {
        val currentDepth = depth
        val newDepth = currentDepth - count
        elements.fill(null, newDepth, currentDepth)
        depth = newDepth
    }

    public fun onlyTop(count: Int) {
        val elements = elements
        val currentDepth = depth
        elements.copyInto(elements, 0, currentDepth - count, currentDepth)
        elements.fill(null, count, currentDepth)
        depth = count
    }

    public fun rot() {
        val elements = elements
        val depth = depth
        val aIndex = depth - 3
        val bIndex = depth - 2
        val cIndex = depth - 1
        val a = elements[aIndex]
        val b = elements[bIndex]
        val c = elements[cIndex]
        elements[aIndex] = b
        elements[bIndex] = c
        elements[cIndex] = a
    }

    public fun rotRev() {
        val elements = elements
        val depth = depth
        val aIndex = depth - 3
        val bIndex = depth - 2
        val cIndex = depth - 1
        val a = elements[aIndex]
        val b = elements[bIndex]
        val c = elements[cIndex]
        elements[aIndex] = c
        elements[bIndex] = a
        elements[cIndex] = b
    }

    public fun swap(first: Int, second: Int) {
        val elements = elements
        val depth = depth
        val firstIndex = depth - 1 - first
        val secondIndex = depth - 1 - second
        val firstValue = elements[firstIndex]
        val secondValue = elements[secondIndex]
        elements[firstIndex] = secondValue
        elements[secondIndex] = firstValue
    }

    /**
     * Swaps blocks (0...j-1) and (j...j+i-1)
     * e.g. block_swap(i=2, j=4): (8 7 6 {5 4} {3 2 1 0} -> 8 7 6 {3 2 1 0} {5 4})
     */
    public fun blockSwap(i: Int, j: Int) {
        val elements = elements
        val currentDepth = depth
        val lastElements = elements.copyOfRange(currentDepth - j, currentDepth)
        elements.copyInto(elements, currentDepth - i, currentDepth - j - i, currentDepth - j)
        lastElements.copyInto(elements, currentDepth - i - j)
    }

    public fun blockDrop(i: Int, j: Int) {
        val elements = elements
        val currentDepth = depth
        val newDepth = currentDepth - i
        elements.copyInto(elements, newDepth - j, currentDepth - j, currentDepth)
        elements.fill(null, newDepth, currentDepth)
        depth = newDepth
    }

    public fun copy(srcSlot: Int, dstSlot: Int) {
        val elements = elements
        val value = elements[srcSlot]
        elements[dstSlot] = value
    }

    public fun popInt(): BigInt = pop() as BigInt

    public fun popBoolean(): Boolean = popInt().sign != 0

    public fun popSlice(): CellSlice = pop() as CellSlice

    override fun toString(): String = buildString {
        val elements = elements
        append("[ ")
        for (i in 0 until depth) {
            append(elements[i])
            append(" ")
        }
        append("] | ${elements.copyOf(16).contentToString()}")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as Stack
        if (depth != other.depth) return false
        if (!elements.contentEquals(other.elements)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = depth
        result = 31 * result + elements.contentHashCode()
        return result
    }
}

/*
package org.ton.kotlin.examples.executor

import org.ton.bigint.BigInt
import org.ton.bigint.sign
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice


class Stack private constructor(
    private val indexedTags: ByteArray,
    private val indexedValues: Array<Any?>
) {
    constructor(size: Int = 256) : this(ByteArray(size), arrayOfNulls(size))

    constructor(values: List<Any>) : this(256) {
        values.forEachIndexed { index, v ->
            when(v) {
                is BigInt -> setInt(index, v)
                is Number -> setInt(index, v.toLong().toBigInteger())
                is CellSlice -> setSlice(index, v)
                is Cell -> setCell(index, v)
                is List<*> -> setTuple(index, v)
                is TvmContinuation -> setCont(index, v)
                else -> throw IllegalArgumentException("Unsupported type ${v::class}")
            }
        }
    }

    fun getBoolean(slot: Int): Boolean {
        verifyIndexedGet(slot, INT_TAG)
        return (indexedValues[slot] as BigInt).sign != 0
    }

    fun setBoolean(slot: Int, value: Boolean) {
        verifyIndexedSet(slot, INT_TAG)
        indexedValues[slot] = if (value) TRUE else FALSE
    }

    fun getInt(slot: Int): BigInt {
        verifyIndexedGet(slot, INT_TAG)
        return indexedValues[slot] as BigInt
    }

    fun setInt(slot: Int, value: BigInt) {
        verifyIndexedSet(slot, INT_TAG)
        indexedValues[slot] = value
    }

    fun setNaN(slot: Int) {
        verifyIndexedSet(slot, NAN_TAG)
        indexedValues[slot] = null
    }

    fun setNull(slot: Int) {
        verifyIndexedSet(slot, NULL_TAG)
        indexedValues[slot] = null
    }

    fun getCell(slot: Int): Cell {
        verifyIndexedGet(slot, CELL_TAG)
        return indexedValues[slot] as Cell
    }

    fun setCell(slot: Int, value: Cell) {
        verifyIndexedSet(slot, CELL_TAG)
        indexedValues[slot] = value
    }

    fun getSlice(slot: Int): CellSlice {
        verifyIndexedGet(slot, SLICE_TAG)
        return indexedValues[slot] as CellSlice
    }

    fun setSlice(slot: Int, value: CellSlice) {
        verifyIndexedSet(slot, CELL_TAG)
        indexedValues[slot] = value
    }

    fun getBuilder(slot: Int): CellBuilder {
        verifyIndexedGet(slot, CELL_TAG)
        return indexedValues[slot] as CellBuilder
    }

    fun setBuilder(slot: Int, value: CellBuilder) {
        verifyIndexedSet(slot, CELL_TAG)
        indexedValues[slot] = value
    }

    fun getCont(slot: Int): TvmContinuation {
        verifyIndexedGet(slot, CONT_TAG)
        return indexedValues[slot] as TvmContinuation
    }

    fun setCont(slot: Int, value: TvmContinuation) {
        verifyIndexedSet(slot, CONT_TAG)
        indexedValues[slot] = value
    }

    fun getTuple(slot: Int): List<Any?> {
        verifyIndexedGet(slot, TUPLE_TAG)
        return indexedValues[slot] as List<Any?>
    }

    fun setTuple(slot: Int, value: List<Any?>) {
        verifyIndexedSet(slot, TUPLE_TAG)
        indexedValues[slot] = value
    }


    fun clear(slot: Int) {
        indexedTags[slot] = 0
        indexedValues[slot] = null
    }

    fun swap(first: Int, second: Int) {
        val indexedValues = indexedValues
        val indexedTags = indexedTags
        val firstTag = indexedTags[first]
        val firstValue = indexedValues[first]

        val secondTag = indexedTags[second]
        val secondValue = indexedValues[second]

        verifyIndexedSet(first, secondTag)
        verifyIndexedSet(second, firstTag)
        indexedValues[first] = secondValue
        indexedValues[second] = firstValue
    }

    fun copy(srcSlot: Int, dstSlot: Int) {
        val indexedTags = indexedTags
        val indexedValues = indexedValues

        val tag = indexedTags[srcSlot]
        val value = indexedValues[srcSlot]

        verifyIndexedSet(dstSlot, tag)
        indexedValues[dstSlot] = value
    }

    fun dump(top: Int): String {
        return buildString {
            append("[ ")
            for (i in 0 until top) {
                append("${dumpValue(i)} ")
            }
            append("]")
        }
    }

    fun dumpValue(slot: Int): String {
        return when(indexedTags[slot]) {
            else -> indexedValues[slot].toString()
        }
    }

    private fun verifyIndexedGet(slot: Int, expectedTag: Byte): Boolean {
        val actualTag = indexedTags[slot]
        val condition = actualTag == expectedTag
        if (!condition) {
            throw frameSlotTypeException(slot, expectedTag, actualTag)
        }
        return true
    }

    private fun verifyIndexedSet(slot: Int, tag: Byte) {
        indexedTags[slot] = tag
    }


    companion object {
        const val NULL_TAG = 0.toByte()
        const val INT_TAG = 1.toByte()
        const val NAN_TAG = 2.toByte()
        const val CELL_TAG = 3.toByte()
        const val SLICE_TAG = 4.toByte()
        const val BUILDER_TAG = 5.toByte()
        const val CONT_TAG = 6.toByte()
        const val TUPLE_TAG = 7.toByte()

        val TRUE = BigInt.ONE.inv()
        val FALSE = BigInt.ZERO

        private fun frameSlotTypeException(slot: Int, expectedTag: Byte, actualTag: Byte): Throwable {
            return IllegalStateException("Stack slot kind $expectedTag expected, byt got $actualTag at stack slot index $slot.")
        }
    }
}
 */

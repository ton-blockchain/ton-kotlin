package org.ton.kotlin.tvm

import org.ton.bigint.BigInt
import org.ton.bigint.sign
import org.ton.bigint.toBigInt
import org.ton.kotlin.tvm.exception.StackOverflowException
import org.ton.kotlin.tvm.exception.StackUnderflowException

private const val INCREMENT = 256

public fun stackOf(vararg elements: Any): Stack {
    val stack = Stack()
    for (element in elements) {
        stack.pushElement(element)
    }
    return stack
}

public class Stack {
    private var elements = arrayOfNulls<Any?>(INCREMENT)
    public var top: Int = -1

    public val depth: Int get() = top + 1

    public operator fun get(offset: Int): Any? {
        if (offset < 0 || offset > depth) {
            throw StackUnderflowException()
        }
        return elements[top - offset]
    }

    public operator fun set(offset: Int, operand: Any) {
        if (offset < 0) {
            throw StackUnderflowException()
        } else if (offset > top) {
            throw StackOverflowException()
        }
        elements[top - offset] = operand
    }

    public fun pop(): Any {
        if (top < 0) {
            throw StackUnderflowException()
        }
        val elements = elements
        val removed = elements[top]
        elements[top--] = null
        return removed as Any
    }

    public fun pushInt(value: Long) {
        pushElement(value.toBigInt())
    }

    public fun pushInt(value: BigInt) {
        pushElement(value)
    }

    public fun pushElement(operand: Any) {
        val nextTop = top + 1
        var elements = elements
        val currentCapacity = elements.size
        if (nextTop >= currentCapacity) {
            elements = elements.copyOf(currentCapacity + INCREMENT)
            this.elements = elements
        }
        elements[nextTop] = operand
        top = nextTop
    }

    public fun pushCopy(offset: Int) {
        var elements = elements
        val value = elements[top - offset]
        val nextTop = top + 1
        val currentCapacity = elements.size
        if (nextTop >= currentCapacity) {
            elements = elements.copyOf(currentCapacity + INCREMENT)
            this.elements = elements
        }
        elements[nextTop] = value
        top = nextTop
    }

    public fun reverse(fromOffset: Int, toOffset: Int) {
        val length = toOffset - fromOffset
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
        elements.fill(null, top - count + 1, top + 1)
        top -= count
    }

    public fun rot() {
        val elements = elements
        val a = elements[top - 2]
        val b = elements[top - 1]
        val c = elements[top]
        elements[top - 2] = b
        elements[top - 1] = c
        elements[top] = a
    }

    public fun rotRev() {
        val elements = elements
        val a = elements[top - 2]
        val b = elements[top - 1]
        val c = elements[top]
        elements[top - 2] = c
        elements[top - 1] = a
        elements[top] = b
    }

    public fun swap(first: Int, second: Int) {
        val elements = elements
        val firstValue = elements[top - first]
        val secondValue = elements[top - second]
        elements[top - first] = secondValue
        elements[top - second] = firstValue
    }

    /**
     * Swaps blocks (0...j-1) and (j...j+i-1)
     * e.g. block_swap(i=2, j=4): (8 7 6 {5 4} {3 2 1 0} -> 8 7 6 {3 2 1 0} {5 4})
     */
    public fun blockSwap(i: Int, j: Int) {
        val elements = elements
        val lastElements = elements.copyOfRange(top - j + 1, top + 1)
        elements.copyInto(elements, top - i + 1, top - j - i + 1, top - j + 1)
        lastElements.copyInto(elements, top - i - j + 1)
    }

    public fun copy(srcSlot: Int, dstSlot: Int) {
        val elements = elements
        val value = elements[srcSlot]
        elements[dstSlot] = value
    }

    public fun popInt(): BigInt = pop() as BigInt

    public fun popBoolean(): Boolean = popInt().sign != 0

    override fun toString(): String = buildString {
        val elements = elements
        append("[ ")
        for (i in 0..top) {
            append(elements[i])
            append(" ")
        }
        append("]")
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

@file:Suppress("NOTHING_TO_INLINE")

package org.ton.fift

import org.ton.kotlin.bigint.BigInt
import org.ton.kotlin.bigint.toBigInt
import org.ton.kotlin.cell.Cell
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice

class Stack(
    val fift: FiftInterpretator,
    private val storage: ArrayDeque<Any> = ArrayDeque()
) : Iterable<Any> {
    val depth: Int get() = storage.size
    val isEmpty: Boolean get() = depth == 0

    override fun iterator(): Iterator<Any> = storage.iterator()

    fun push(stackEntry: Any) {
        when (stackEntry) {
            is Int -> push(stackEntry.toBigInt())
            is Long -> push(stackEntry.toBigInt())
            is Boolean -> push(if (stackEntry) (-1).toBigInt() else 0.toBigInt())
            else -> {
                fift.logger.debug { "${fift.debugExecutionDepthIndent()}Stack push: ${stackEntry.fiftFormat()}" }
                storage.addLast(stackEntry)
            }
        }
    }

    fun pop(): Any = try {
        val value = storage.removeLast()
        fift.logger.debug { "${fift.debugExecutionDepthIndent()}Stack pop: ${value.fiftFormat()}" }
        value
    } catch (e: NoSuchElementException) {
        throw FiftStackOverflow()
    }

    fun pop(index: Int): Any = try {
        val value = storage.removeAt(storage.lastIndex - index)
        fift.logger.debug { "${fift.debugExecutionDepthIndent()}Stack pop at $index: ${value.fiftFormat()}" }
        value
    } catch (e: NoSuchElementException) {
        throw FiftStackOverflow()
    }

    operator fun get(index: Int = 0): Any = try {
        val value = getStackEntry(index)
        fift.logger.debug { "${fift.debugExecutionDepthIndent()}Stack set at $index: ${value.fiftFormat()}" }
        value
    } catch (e: NoSuchElementException) {
        throw FiftStackOverflow()
    }

    private fun getStackEntry(index: Int) = storage[storage.lastIndex - index]

    operator fun set(index: Int, stackEntry: Any) {
        fift.logger.debug { "${fift.debugExecutionDepthIndent()}Stack set at $index: ${stackEntry.fiftFormat()}" }
        setStackEntry(index, stackEntry)
    }

    private fun setStackEntry(index: Int, stackEntry: Any) {
        storage[storage.lastIndex - index] = stackEntry
    }

    fun clear() {
        fift.logger.debug { "${fift.debugExecutionDepthIndent()}Stack clear" }
        storage.clear()
    }

    fun swap(firstIndex: Int, secondIndex: Int) {
        fift.logger.debug { "${fift.debugExecutionDepthIndent()}Stack swap $firstIndex<->$secondIndex" }
        val swap = getStackEntry(firstIndex)
        setStackEntry(firstIndex, getStackEntry(secondIndex))
        setStackEntry(secondIndex, swap)
    }

}

fun Stack.pushArgCount(args: Int) {
    push(args)
    push(NopWordDef)
}

fun Stack.popInt() = pop() as BigInt
fun Stack.popString() = pop() as String
fun Stack.popWordList() = pop() as WordList
fun Stack.popWordDef() = pop() as WordDef
fun Stack.popBox() = pop() as Box
fun Stack.popCell() = pop() as Cell
fun Stack.popCellSlice() = pop() as CellSlice
fun Stack.popCellBuilder() = pop() as CellBuilder

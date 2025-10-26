@file:Suppress("NOTHING_TO_INLINE")

package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.bigint.BigInt
import org.ton.bigint.toInt
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb
import kotlin.jvm.JvmStatic

public inline fun VmStack(depth: Int, stack: VmStackList): VmStack = VmStackImpl(depth, stack)
public inline fun VmStack(stack: VmStackList): VmStack = VmStackImpl(stack)

public interface VmStack : Collection<VmStackValue> {
    public val depth: Int
    public val stack: VmStackList

    override fun iterator(): Iterator<VmStackValue> = stack.iterator()
    override fun contains(element: VmStackValue): Boolean = stack.contains(element)
    override fun containsAll(elements: Collection<VmStackValue>): Boolean = elements.all {
        stack.contains(it)
    }

    override val size: Int get() = depth
    override fun isEmpty(): Boolean = depth <= 0

    public fun toMutableVmStack(): MutableVmStack

    public operator fun get(index: Int): VmStackValue {
        forEachIndexed { currentIndex, vmStackValue ->
            if (currentIndex == index) {
                return vmStackValue
            }
        }
        throw IllegalArgumentException("index: $index")
    }

    public companion object : TlbCodec<VmStack> by VmStackTlbConstructor {
        @JvmStatic
        public fun tlbCodec(): TlbConstructor<VmStack> = VmStackTlbConstructor
    }
}

public interface MutableVmStack : VmStack {
    public fun pop(): VmStackValue
    public fun popNull(): VmStackNull = pop() as VmStackNull
    public fun popTinyInt(): Long = popNumber().toLong()
    public fun popBool(): Boolean = popTinyInt() != 0L
    public fun popInt(): BigInt = popNumber().toBigInt()
    public fun popNumber(): VmStackNumber = (pop() as VmStackNumber)
    public fun popCell(): Cell = (pop() as VmStackCell).cell
    public fun popSlice(): CellSlice = (pop() as VmStackSlice).toCellSlice()
    public fun popBuilder(): CellBuilder = (pop() as VmStackBuilder).toCellBuilder()
    public fun popCont(): VmCont = (pop() as VmStackCont).cont
    public fun popTuple(): VmTuple = (pop() as VmStackTuple).data

    public fun push(stackValue: VmStackValue)
    public fun pushNull(): Unit = push(VmStackNull)
    public fun pushTinyInt(tinyInt: Boolean): Unit = push(VmStackValue(tinyInt))
    public fun pushTinyInt(tinyInt: Int): Unit = push(VmStackValue(tinyInt))
    public fun pushTinyInt(tinyInt: Long): Unit = push(VmStackValue(tinyInt))
    public fun pushBool(boolean: Boolean): Unit = push(VmStackValue(boolean))
    public fun pushInt(int: BigInt): Unit = push(VmStackValue(int))
    public fun pushNan(): Unit = push(VmStackNan)
    public fun pushCell(cell: Cell): Unit = push(VmStackValue(cell))
    public fun pushSlice(cellSlice: CellSlice): Unit = push(VmStackValue(cellSlice))
    public fun pushBuilder(cellBuilder: CellBuilder): Unit = push(VmStackValue(cellBuilder))
    public fun pushCont(vmCont: VmCont): Unit = push(VmStackValue(vmCont))
    public fun pushTuple(vmTuple: VmTuple): Unit = push(VmStackValue(vmTuple))

    public fun interchange(i: Int, j: Int)
    public fun interchange(i: Int): Unit = interchange(0, i)
    public fun swap(): Unit = interchange(0, 1)
}

@SerialName("vm_stack")

public data class VmStackImpl(
    override val depth: Int,
    override val stack: VmStackList
) : VmStack {
    public constructor(stack: VmStackList) : this(stack.count(), stack)

    override fun toMutableVmStack(): MutableVmStack = MutableVmStackImpl(stack)

    override fun toString(): String = "(vm_stack depth:$depth stack:$stack)"
}

public inline fun MutableVmStack(): MutableVmStack = MutableVmStackImpl()

public class MutableVmStackImpl(
    iterable: Iterable<VmStackValue> = emptyList()
) : MutableVmStack {
    private val _stack = ArrayDeque<VmStackValue>().also { it.addAll(iterable) }
    override val depth: Int get() = _stack.size

    override val stack: VmStackList get() = VmStackList(_stack)
    override fun get(index: Int): VmStackValue = _stack[index]

    override fun pop(): VmStackValue = _stack.removeLast()

    override fun push(stackValue: VmStackValue) {
        _stack.addLast(stackValue)
    }

    override fun toMutableVmStack(): MutableVmStack = this

    override fun interchange(i: Int, j: Int) {
        val iStackValue = _stack[i]
        val jStackValue = _stack[j]
        _stack[i] = jStackValue
        _stack[j] = iStackValue
    }

    override fun toString(): String = "(vm_stack depth:$depth stack:$stack)"
}

private object VmStackTlbConstructor : TlbConstructor<VmStack>(
    schema = "vm_stack#_ depth:(## 24) stack:(VmStackList depth) = VmStack;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: VmStack
    ) = cellBuilder {
        storeUInt(value.depth, 24)
        storeTlb(VmStackList.tlbCodec(value.depth), value.stack)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmStack = cellSlice {
        val depth = loadUInt(24).toInt()
        val stack = loadTlb(VmStackList.tlbCodec(depth))
        VmStackImpl(depth, stack)
    }
}

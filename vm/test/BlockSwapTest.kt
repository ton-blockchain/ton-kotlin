package org.ton.kotlin.tvm

import org.ton.bitstring.BitString
import org.ton.cell.buildCell
import kotlin.test.Test

class BlockSwapTest {
    @Test
    fun testBlockSwap() {
        //      * e.g. block_swap(i=2, j=4): (8 7 6 {5 4} {3 2 1 0} -> 8 7 6 {3 2 1 0} {5 4})
        val stack = Stack()
        stack.pushInt(8)
        stack.pushInt(7)
        stack.pushInt(6)
        stack.pushInt(5)
        stack.pushInt(4)
        stack.pushInt(3)
        stack.pushInt(2)
        stack.pushInt(1)
        stack.pushInt(0)
        println(stack)
        stack.blockSwap(2, 4)
        println(stack)
    }

    @Test
    fun testRot() {
        val stack = Stack()
        stack.pushInt(0)
        stack.pushInt(1)
        stack.pushInt(2)
        stack.pushInt(3)
        println(stack)
        stack.rot()
        println(stack)
    }

    @Test
    fun drop2() {
        val stack = Stack()
        stack.pushInt(0)
        stack.pushInt(1)
        stack.pushInt(2)
        stack.pushInt(3)
        println(stack)
        stack.dropTop(2)
        println(stack)
    }

    @Test
    fun reverse() {
        val stack = stackOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
        val tvm = Tvm()
        tvm.execute(stack, buildCell {
            storeBitString(BitString("5E325E33"))
        }.beginParse())
        println(stack)
    }
}

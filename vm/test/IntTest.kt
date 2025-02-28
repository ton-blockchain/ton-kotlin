package org.ton.kotlin.tvm

import org.ton.bigint.pow
import org.ton.bigint.toBigInt
import org.ton.bitstring.BitString
import org.ton.cell.buildCell
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IntTest {
    @Test
    fun testPushIntLong() {
        val stack = stackOf()
        Tvm().execute(stack, buildCell {
            storeBitString(BitString("821005F5E100"))
        })
        assertEquals(10.toBigInt().pow(8), stack.popInt())
    }

    @Test
    fun testPushPow2() {
        val stack = stackOf(10)
        Tvm().execute(stack, buildCell {
            storeBitString(BitString("8308"))
        })
        assertEquals(512.toBigInt(), stack.popInt())
    }

    @Test
    fun testPushNan() {
        val stack = stackOf()
        Tvm().execute(stack, buildCell {
            storeBitString(BitString("83FF"))
        })
        assertTrue(stack.popInt().isNan())
    }

    @Test
    fun testPushPow2Dec() {
        val stack = stackOf()
        Tvm().execute(stack, buildCell {
            storeBitString(BitString("8408"))
        })
        assertEquals(511.toBigInt(), stack.popInt())
    }

    @Test
    fun testPushNegPow2() {
        val stack = stackOf()
        Tvm().execute(stack, buildCell {
            storeBitString(BitString("8508"))
        })
        assertEquals((-512).toBigInt(), stack.popInt())
    }
}

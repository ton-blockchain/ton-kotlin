package org.ton.kotlin.tvm

import org.ton.bitstring.BitString
import org.ton.cell.buildCell
import kotlin.test.Test
import kotlin.test.assertEquals

class ConstTest {
    @Test
    fun testPushSliceEmpty() {
        val stack = stackOf()
        Tvm().execute(stack, buildCell {
            storeBitString(BitString("8B08"))
        })
        val slice = stack.popSlice()
        slice.endParse()
    }

    @Test
    fun testPushSlice0() {
        val stack = stackOf()
        Tvm().execute(stack, buildCell {
            storeBitString(BitString("8B04"))
        })
        val slice = stack.popSlice()
        assertEquals(BitString.binary("0"), slice.loadBitString(1))
        slice.endParse()
    }

    @Test
    fun testPushSlice1() {
        val stack = stackOf()
        Tvm().execute(stack, buildCell {
            storeBitString(BitString("8B0C"))
        })
        val slice = stack.popSlice()
        assertEquals(BitString.binary("1"), slice.loadBitString(1))
        slice.endParse()
    }
}

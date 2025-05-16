//package org.ton.fift
//
//import org.ton.kotlin.bigint.BigInt
//import kotlin.test.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertTrue
//
//class WordsTest {
//    @Test
//    fun testBasicArithmetic() = testFift {
//        interpret("7 4 -")
//        assertEquals(BigInt(3), stack.popInt())
//        interpret("2 3 4 * +")
//        interpret("2 3 + 4 *")
//        assertEquals(BigInt(20), stack.popInt())
//        assertEquals(BigInt(14), stack.popInt())
//    }
//
//    @Test
//    fun `test word plus`() = testFift {
//        interpret("1 2 +")
//        assertEquals(BigInt(3), stack.popInt())
//        interpret("5 -10 +")
//        assertEquals(BigInt(-5), stack.popInt())
//        interpret("42 37 94 + + 11 +")
//        assertEquals(BigInt(184), stack.popInt())
//        assertTrue(stack.isEmpty)
//    }
//
//    @Test
//    fun `test word minus`() = testFift {
//        interpret("5 3 -")
//        assertEquals(BigInt(2), stack.popInt())
//        interpret("-10 -3 -")
//        assertEquals(BigInt(-7), stack.popInt())
//        interpret("-19 397 -24 - - 33 -")
//        assertEquals(BigInt(-473), stack.popInt())
//        assertTrue(stack.isEmpty)
//    }
//
//    @Test
//    fun `test word negate`() = testFift {
//        interpret("53 negate")
//        assertEquals(BigInt(-53), stack.popInt())
//        interpret("-10 negate")
//        assertEquals(BigInt(10), stack.popInt())
//        interpret("397 -24 negate negate 99 negate 11")
//        assertEquals(BigInt(11), stack.popInt())
//        assertEquals(BigInt(-99), stack.popInt())
//        assertEquals(BigInt(-24), stack.popInt())
//        assertEquals(BigInt(397), stack.popInt())
//    }
//
//    @Test
//    fun testDup() = testFift {
//        interpret("1 2 dup")
//        assertEquals(BigInt(2), stack.popInt())
//        assertEquals(BigInt(2), stack.popInt())
//        assertEquals(BigInt(1), stack.popInt())
//    }
//
//    @Test
//    fun testDrop() = testFift {
//        interpret("3 1 2 drop")
//        assertEquals(BigInt(1), stack.popInt())
//        interpret("drop")
//    }
//
//    @Test
//    fun testSwap() = testFift {
//        interpret("1 2 3 swap")
//        assertEquals(BigInt(2), stack.popInt())
//        assertEquals(BigInt(3), stack.popInt())
//        assertEquals(BigInt(1), stack.popInt())
//    }
//
//    @Test
//    fun testRot() = testFift {
//        interpret("1 2 3 rot")
//        assertEquals(BigInt(1), stack.popInt())
//        assertEquals(BigInt(3), stack.popInt())
//        assertEquals(BigInt(2), stack.popInt())
//    }
//
//    @Test
//    fun testNegRot() = testFift {
//        interpret("1 2 3 -rot")
//        assertEquals(BigInt(2), stack.popInt())
//        assertEquals(BigInt(1), stack.popInt())
//        assertEquals(BigInt(3), stack.popInt())
//        interpret("1 2 3 rot rot")
//        assertEquals(BigInt(2), stack.popInt())
//        assertEquals(BigInt(1), stack.popInt())
//        assertEquals(BigInt(3), stack.popInt())
//    }
//
//    @Test
//    fun testOver() = testFift {
//        interpret("1 2 over")
//        assertEquals(BigInt(1), stack.popInt())
//        assertEquals(BigInt(2), stack.popInt())
//        assertEquals(BigInt(1), stack.popInt())
//    }
//
//    @Test
//    fun testTuck() = testFift {
//        interpret("1 2 tuck")
//        assertEquals(BigInt(2), stack.popInt())
//        assertEquals(BigInt(1), stack.popInt())
//        assertEquals(BigInt(2), stack.popInt())
//        interpret("1 2 swap over")
//        assertEquals(BigInt(2), stack.popInt())
//        assertEquals(BigInt(1), stack.popInt())
//        assertEquals(BigInt(2), stack.popInt())
//    }
//
//    @Test
//    fun testNip() = testFift {
//        interpret("1 2 nip")
//        assertEquals(BigInt(2), stack.popInt())
//        interpret("1 2 swap drop")
//        assertEquals(BigInt(2), stack.popInt())
//    }
//
//    @Test
//    fun test2dup() = testFift {
//        interpret("1 2 2dup")
//        assertEquals(BigInt(2), stack.popInt())
//        assertEquals(BigInt(1), stack.popInt())
//        assertEquals(BigInt(2), stack.popInt())
//        assertEquals(BigInt(1), stack.popInt())
//        interpret("1 2 over over")
//        assertEquals(BigInt(2), stack.popInt())
//        assertEquals(BigInt(1), stack.popInt())
//        assertEquals(BigInt(2), stack.popInt())
//        assertEquals(BigInt(1), stack.popInt())
//    }
//
//    @Test
//    fun test2drop() = testFift {
//        interpret("1 2 2drop")
//        assertTrue(stack.isEmpty)
//        interpret("1 2 drop drop")
//        assertTrue(stack.isEmpty)
//    }
//
//    @Test
//    fun test2swap() = testFift {
//        interpret("1 2 3 4 2swap")
//        assertEquals(BigInt(2), stack.popInt())
//        assertEquals(BigInt(1), stack.popInt())
//        assertEquals(BigInt(4), stack.popInt())
//        assertEquals(BigInt(3), stack.popInt())
//    }
//
//    @Test
//    fun testPick() = testFift {
//        interpret("1 2 3")
//        interpret("0 pick")
//        assertEquals(BigInt(3), stack.popInt())
//        interpret("1 pick")
//        assertEquals(BigInt(2), stack.popInt())
//        interpret("2 pick")
//        assertEquals(BigInt(1), stack.popInt())
//        repeat(stack.depth) {
//            stack.pop()
//        }
//    }
//}
//
//private fun testFift(block: FiftInterpretator.() -> Unit) {
//    val fift = FiftInterpretator(output = {})
//    fift.apply(block)
//    assertTrue(fift.stack.isEmpty)
//}

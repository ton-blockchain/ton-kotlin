package org.ton.hashmap

import org.ton.bitstring.BitString
import org.ton.cell.Cell
import org.ton.hashmap.tlb.testSerialization
import org.ton.tlb.loadNegatedTlb
import kotlin.test.Test
import kotlin.test.assertEquals

class UnaryTest {
    @Test
    fun `test serialization Unary`() {
        val codec = Unary.tlbCodec()

        testSerialization(codec, UnaryZero)
        testSerialization(codec, UnarySuccess(UnaryZero))
        testSerialization(codec, UnarySuccess(UnarySuccess(UnaryZero)))
        testSerialization(codec, UnarySuccess(UnarySuccess(UnarySuccess(UnaryZero))))
        testSerialization(codec, UnarySuccess(UnarySuccess(UnarySuccess(UnarySuccess(UnaryZero)))))

        Cell(BitString.binary("1111111100101")).parse {
            val (depth, result) = loadNegatedTlb(codec)
            assertEquals(BitString.binary("0101"), loadBitString(4))
            assertEquals(Unary(8), result)
            assertEquals(8, depth)
        }
    }
}

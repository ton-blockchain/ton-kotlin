package org.ton.kotlin.block

import org.ton.bigint.BigInt
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.storeRef
import kotlin.random.Random
import kotlin.test.Test

class VmStackValueTest {
    val codec = VmStackValue.tlbCombinator()

    @Test
    fun `test VmStackValue deserialization`() {

        testSerialization(codec, VmStackNull)

        testSerialization(codec, VmStackTinyInt(17239))
        testSerialization(codec, VmStackTinyInt(-17))
        testSerialization(codec, VmStackTinyInt(1000000239))
        testSerialization(codec, VmStackTinyInt(1000000239L * 1000000239))
        repeat(10) {
            testSerialization(codec, VmStackTinyInt(Random.nextLong()))
        }

        testSerialization(codec, VmStackInt(17239))
        testSerialization(codec, VmStackInt(-17))
        testSerialization(codec, VmStackInt(1000000239))
        testSerialization(codec, VmStackInt(1000000239L * 1000000239))
        testSerialization(codec, VmStackInt(BigInt("-1000000000000000000000000239")))

        repeat(10) {
            testSerialization(codec, VmStackInt(Random.nextLong()))
            testSerialization(codec, VmStackInt(BigInt(Random.nextBytes(256 / 8))))
        }

        testSerialization(
            codec, VmStackCell(
                CellBuilder.createCell {
                    storeBits(BitString("989A386C05EFF862FFFFE23_"))
                    storeRef {
                        storeBits(BitString("00000001BC16E45E4D41643_"))
                    }
                    storeRef {
                        storeBits(BitString("3B9ACAEF"))
                        storeRef {
                            storeBits(BitString("FDF_"))
                        }
                    }
                }
            ))
    }


}

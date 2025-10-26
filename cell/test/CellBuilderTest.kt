package org.ton.sdk.cell

import org.ton.sdk.bigint.toBigInt
import org.ton.sdk.bitstring.BitString
import kotlin.test.Test

class CellBuilderTest {
    @Test
    fun foo() {
        val cb1 = CellBuilder()
        val cb2 = CellBuilder()

        cb1.store("Hello, ".encodeToByteArray())

        cb2.store(BitString(ByteArray(1), 4))
            .storeLong(17239, 16)
            .storeLong(-17, 11)
            .storeLong(1000000239, 32)
            .storeLong(1000000239L * 1000000239)
            .storeUBigInt("-1000000000000000000000000239".toBigInt(), 91)

        cb2.store("<->".encodeToByteArray())
        val c2 = cb2.build()
        println(c2)
    }
}

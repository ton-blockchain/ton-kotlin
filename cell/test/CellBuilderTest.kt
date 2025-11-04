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
            .store(BitString("world!".encodeToByteArray(), 48))

        cb2
            .store(BitString(byteArrayOf(0xd0.toByte()), 4))
            .storeLong(17239, 16)
            .storeLong(-17, 11)
            .storeLong(1000000239, 32)
            .storeLong(1000000239L * 1000000239)
            .storeBigInt("-1000000000000000000000000239".toBigInt(), 91)

        println("hash=${cb1.build().hash()}; c1=${cb1.build()}")
        println("hash=${cb2.build().hash()}; c2=${cb2.build()}")

        cb1.storeReference(cb2.build())
        cb2.store("<->".encodeToByteArray())

        val c1 = cb1.build()
        val c2 = cb2.build()
        println("hash=${c1.hash()}; c1=${c1}")
        println("hash=${c2.hash()}; c1=${c2}")
    }
}

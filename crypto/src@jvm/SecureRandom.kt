@file:JvmName("SecureRandomJvmKt")

package org.ton.crypto

import kotlin.random.Random

public actual object SecureRandom : Random() {
    private val javaSecureRandom = java.security.SecureRandom()

    override fun nextBits(bitCount: Int): Int = nextInt().takeUpperBits(bitCount)

    override fun nextInt(): Int = javaSecureRandom.nextInt()

    override fun nextBytes(array: ByteArray, fromIndex: Int, toIndex: Int): ByteArray {
        val tmp = ByteArray(toIndex - fromIndex)
        javaSecureRandom.nextBytes(tmp)
        tmp.copyInto(array, fromIndex)
        return array
    }
}

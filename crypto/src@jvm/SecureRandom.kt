@file:JvmName("SecureRandomJvmKt")

package org.ton.kotlin.crypto

import kotlin.random.Random

public actual object SecureRandom : Random() {
    private val javaSecureRandom = java.security.SecureRandom()

    actual override fun nextBits(bitCount: Int): Int = nextInt().takeUpperBits(bitCount)

    override fun nextInt(): Int = javaSecureRandom.nextInt()

    actual override fun nextBytes(array: ByteArray, fromIndex: Int, toIndex: Int): ByteArray {
        val tmp = ByteArray(toIndex - fromIndex)
        javaSecureRandom.nextBytes(tmp)
        tmp.copyInto(array, fromIndex)
        return array
    }
}

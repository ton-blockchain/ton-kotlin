package org.ton.kotlin.crypto

import kotlin.random.Random

public expect object SecureRandom : Random {
    override fun nextBits(bitCount: Int): Int

    override fun nextBytes(array: ByteArray, fromIndex: Int, toIndex: Int): ByteArray
}

internal inline fun Int.takeUpperBits(bitCount: Int): Int =
    this.ushr(32 - bitCount) and (-bitCount).shr(31)

package org.ton.sdk.crypto

import kotlin.random.Random

public object SecureRandom : Random() {
    override fun nextBits(bitCount: Int): Int {
        return secureRandomNextBits(bitCount)
    }

    override fun nextBytes(array: ByteArray, fromIndex: Int, toIndex: Int): ByteArray {
        return secureRandomNextBytes(array, fromIndex, toIndex)
    }

    override fun nextInt(): Int {
        return secureRandomNextInt()
    }
}

internal expect fun secureRandomNextBits(bitCount: Int): Int
internal expect fun secureRandomNextBytes(array: ByteArray, fromIndex: Int, toIndex: Int): ByteArray
internal expect fun secureRandomNextInt(): Int

@Suppress("NOTHING_TO_INLINE")
internal inline fun Int.takeUpperBits(bitCount: Int): Int =
    this.ushr(32 - bitCount) and (-bitCount).shr(31)

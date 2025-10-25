package org.ton.sdk.crypto

internal actual fun secureRandomNextBits(bitCount: Int): Int {
    return secureRandomNextInt().takeUpperBits(bitCount)
}

internal actual fun secureRandomNextBytes(
    array: ByteArray,
    fromIndex: Int,
    toIndex: Int
): ByteArray {
    val tmp = ByteArray(toIndex - fromIndex)
    java.security.SecureRandom().nextBytes(tmp)
    tmp.copyInto(array, fromIndex)
    return array
}

internal actual fun secureRandomNextInt(): Int {
    return java.security.SecureRandom().nextInt()
}

package org.ton.sdk.crypto

import kotlinx.cinterop.*
import platform.windows.BCRYPT_USE_SYSTEM_PREFERRED_RNG
import platform.windows.BCryptGenRandom

internal actual fun secureRandomNextBits(bitCount: Int): Int {
    return secureRandomNextInt().takeUpperBits(bitCount)
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun secureRandomNextBytes(array: ByteArray, fromIndex: Int, toIndex: Int): ByteArray {
    array.usePinned {
        val result = BCryptGenRandom(
            null,
            it.addressOf(fromIndex).reinterpret(),
            (toIndex - fromIndex).convert(),
            BCRYPT_USE_SYSTEM_PREFERRED_RNG.convert()
        )
        if (result != 0) {
            error("Can't generate random values using BCryptGenRandom: $result")
        }
    }
    return array
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun secureRandomNextInt(): Int = memScoped {
    val int = alloc<IntVar>()
    val result = BCryptGenRandom(
        null,
        int.ptr.reinterpret(),
        4u,
        BCRYPT_USE_SYSTEM_PREFERRED_RNG.convert()
    )
    if (result != 0) {
        error("Can't generate random values using BCryptGenRandom: $result")
    }
    return int.value
}

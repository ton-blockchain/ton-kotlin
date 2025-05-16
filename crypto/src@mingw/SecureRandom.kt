package org.ton.kotlin.crypto

import kotlinx.cinterop.*
import platform.windows.BCRYPT_USE_SYSTEM_PREFERRED_RNG
import platform.windows.BCryptGenRandom
import kotlin.random.Random

@OptIn(ExperimentalForeignApi::class)
public actual object SecureRandom : Random() {
    override fun nextInt(): Int = memScoped {
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

    actual override fun nextBits(bitCount: Int): Int = nextInt().takeUpperBits(bitCount)

    actual override fun nextBytes(array: ByteArray, fromIndex: Int, toIndex: Int): ByteArray {
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
}

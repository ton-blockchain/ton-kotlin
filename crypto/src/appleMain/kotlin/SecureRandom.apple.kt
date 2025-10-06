package org.ton.kotlin.crypto

import kotlinx.cinterop.*
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread

internal actual fun secureRandomNextBits(bitCount: Int): Int {
    return secureRandomNextInt().takeUpperBits(bitCount)
}

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
internal actual fun secureRandomNextBytes(
    array: ByteArray,
    fromIndex: Int,
    toIndex: Int
): ByteArray {
    val fd = fopen("/dev/urandom", "rb") ?: error("Can't open /dev/urandom")
    array.usePinned {
        fread(it.addressOf(fromIndex), 1u, (toIndex - fromIndex).convert(), fd)
    }
    fclose(fd)
    return array
}

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
internal actual fun secureRandomNextInt(): Int = memScoped {
    val file = fopen("/dev/urandom", "rb") ?: error("Can't open /dev/urandom")
    val int = alloc<IntVar>()
    fread(int.ptr, 4.convert(), 1.convert(), file)
    fclose(file)
    return int.value
}

package org.ton.kotlin.crypto.curve25519.internal

internal fun ByteArray.getLongLE(offset: Int = 0): Long {
    return ((this[offset].toUByte().toLong()) or
            (this[offset + 1].toUByte().toLong() shl 8) or
            (this[offset + 2].toUByte().toLong() shl 16) or
            (this[offset + 3].toUByte().toLong() shl 24) or
            (this[offset + 4].toUByte().toLong() shl 32) or
            (this[offset + 5].toUByte().toLong() shl 40) or
            (this[offset + 6].toUByte().toLong() shl 48) or
            (this[offset + 7].toUByte().toLong() shl 56))
}

internal fun ByteArray.getULongLE(index: Int = 0): ULong = getLongLE(index).toULong()

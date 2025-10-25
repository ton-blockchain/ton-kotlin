package org.ton.sdk.crypto

import java.security.MessageDigest

public actual class Sha512 : Digest {
    private val digest = MessageDigest.getInstance("SHA-512")
    actual override val digestSize: Int
        get() = digest.digestLength
    actual override val blockSize: Int
        get() = 128

    public actual override fun update(source: ByteArray, startIndex: Int, endIndex: Int): Sha512 = apply {
        if (endIndex - startIndex == 0) return this
        digest.update(source, startIndex, endIndex - startIndex)
    }

    public actual override fun digest(destination: ByteArray, destinationOffset: Int) {
        digest.digest(destination, destinationOffset, destination.size - destinationOffset)
    }

    public actual override fun reset() {
        digest.reset()
    }

    actual override fun close() {
        digest.reset()
    }
}

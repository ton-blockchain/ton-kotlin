package org.ton.kotlin.crypto

interface Decryptor {
    fun decryptToByteArray(
        source: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ): ByteArray {
        val destination = ByteArray(endIndex - startIndex + 32)
        decryptIntoByteArray(source, destination, 0, startIndex, endIndex)
        return destination
    }

    fun decryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int = 0,
        startIndex: Int = 0,
        endIndex: Int = source.size
    )

    fun signToByteArray(
        source: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ): ByteArray {
        val destination = ByteArray(64)
        signIntoByteArray(source, destination, 0, startIndex, endIndex)
        return destination
    }

    fun signIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int = 0,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ) {
        throw NotImplementedError("signIntoByteArray is not implemented for ${this::class.simpleName}")
    }
}

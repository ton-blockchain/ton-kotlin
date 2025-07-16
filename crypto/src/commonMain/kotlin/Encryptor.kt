package org.ton.kotlin.crypto

interface Encryptor {
    fun encryptToByteArray(
        source: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ): ByteArray {
        val destination = ByteArray(endIndex - startIndex + 32)
        encryptIntoByteArray(source, destination, 0, startIndex, endIndex)
        return destination
    }

    fun encryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int = 0,
        startIndex: Int = 0,
        endIndex: Int = source.size
    )
}

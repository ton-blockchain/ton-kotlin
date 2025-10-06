package org.ton.kotlin.crypto

public interface Encryptor {
    public fun encryptToByteArray(
        source: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ): ByteArray {
        val destination = ByteArray(endIndex - startIndex + 32)
        encryptIntoByteArray(source, destination, 0, startIndex, endIndex)
        return destination
    }

    public fun encryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int = 0,
        startIndex: Int = 0,
        endIndex: Int = source.size
    )

    public fun checkSignature(
        source: ByteArray,
        signature: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ): Boolean
}

public object EncryptorNone : Encryptor {
    override fun encryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ) {
    }

    override fun checkSignature(
        source: ByteArray,
        signature: ByteArray,
        startIndex: Int,
        endIndex: Int
    ): Boolean = true
}

public object EncryptorFail : Encryptor {
    override fun encryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ) {
        throw IllegalStateException("Fail encryptor")
    }

    override fun checkSignature(
        source: ByteArray,
        signature: ByteArray,
        startIndex: Int,
        endIndex: Int
    ): Boolean {
        throw IllegalStateException("Fail encryptor")
    }
}

package org.ton.sdk.crypto

public interface Encryptor {
    public fun encryptToByteArray(
        source: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ): ByteArray

    public fun encryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int = 0,
        startIndex: Int = 0,
        endIndex: Int = source.size
    )
}

public object EncryptorNone : Encryptor {
    override fun encryptToByteArray(
        source: ByteArray,
        startIndex: Int,
        endIndex: Int
    ): ByteArray {
        return source.copyOfRange(startIndex, endIndex)
    }

    override fun encryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ) {
        source.copyInto(destination, destinationOffset, startIndex, endIndex)
    }
}

public object EncryptorFail : Encryptor {
    override fun encryptToByteArray(
        source: ByteArray,
        startIndex: Int,
        endIndex: Int
    ): ByteArray {
        throw IllegalStateException("Fail encryptor")
    }

    override fun encryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ) {
        throw IllegalStateException("Fail encryptor")
    }
}

package org.ton.sdk.crypto

public interface Decryptor {
    public fun decryptToByteArray(
        source: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ): ByteArray

    public fun decryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int = 0,
        startIndex: Int = 0,
        endIndex: Int = source.size
    )
}

public object DecryptorNone : Decryptor {
    override fun decryptToByteArray(
        source: ByteArray,
        startIndex: Int,
        endIndex: Int
    ): ByteArray {
        return source.copyOfRange(startIndex, endIndex)
    }

    override fun decryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ) {
    }
}

public object DecryptorFail : Decryptor {
    override fun decryptToByteArray(
        source: ByteArray,
        startIndex: Int,
        endIndex: Int
    ): ByteArray {
        throw IllegalStateException("Fail decryptor")
    }

    override fun decryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ) {
        throw IllegalStateException("Fail decryptor")
    }
}

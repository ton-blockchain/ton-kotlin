package org.ton.kotlin.crypto

public interface Decryptor {
    public fun decryptToByteArray(
        source: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ): ByteArray {
        val destination = ByteArray(endIndex - startIndex + 32)
        decryptIntoByteArray(source, destination, 0, startIndex, endIndex)
        return destination
    }

    public fun decryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int = 0,
        startIndex: Int = 0,
        endIndex: Int = source.size
    )

    @Deprecated("Use signToByteArray or signIntoByteArray instead", ReplaceWith("signToByteArray(message)"))
    public fun sign(message: ByteArray): ByteArray = signToByteArray(message)

    public fun signToByteArray(
        source: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ): ByteArray {
        val destination = ByteArray(64)
        signIntoByteArray(source, destination, 0, startIndex, endIndex)
        return destination
    }

    public fun signIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int = 0,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ) {
        throw NotImplementedError("signIntoByteArray is not implemented for ${this::class.simpleName}")
    }
}

public object DecryptorNone : Decryptor {
    override fun decryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ) {
    }

    override fun signIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ) {
    }
}

public object DecryptorFail : Decryptor {
    override fun decryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ) {
        throw IllegalStateException("Fail decryptor")
    }

    override fun signIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ) {
        throw IllegalStateException("Fail decryptor")
    }
}

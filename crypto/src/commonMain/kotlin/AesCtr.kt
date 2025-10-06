package org.ton.kotlin.crypto

public expect class AesCtr : AutoCloseable {
    public constructor(key: ByteArray, iv: ByteArray)

    public fun processBytes(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int = 0,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ): Int

    override fun close()
}

public class EncryptorAes(
    private val sharedSecret: ByteArray
) : Encryptor {
    override fun encryptToByteArray(source: ByteArray, startIndex: Int, endIndex: Int): ByteArray {
        val destination = ByteArray(endIndex - startIndex + 32)
        encryptIntoByteArray(source, destination, 0, startIndex, endIndex)
        return destination
    }

    override fun encryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ) {
        val digest = Sha256()
        digest.use {
            digest.update(source, startIndex, endIndex)
            digest.digest(destination, destinationOffset)
        }

        val key = ByteArray(32)
        sharedSecret.copyInto(key, destinationOffset = 0, startIndex = 0, endIndex = 16)
        destination.copyInto(
            key,
            destinationOffset = 16,
            startIndex = destinationOffset + 16,
            endIndex = destinationOffset + 32
        )

        val iv = ByteArray(16)
        destination.copyInto(
            iv,
            destinationOffset = 0,
            startIndex = destinationOffset + 0,
            endIndex = destinationOffset + 4
        )
        sharedSecret.copyInto(iv, destinationOffset = 4, startIndex = 20, endIndex = 32)

        val cipher = AesCtr(key, iv)
        cipher.use {
            cipher.processBytes(source, destination, destinationOffset = destinationOffset + 32, startIndex, endIndex)
        }
    }
}

public class DecryptorAes(
    private val sharedSecret: ByteArray
) : Decryptor {
    override fun decryptToByteArray(source: ByteArray, startIndex: Int, endIndex: Int): ByteArray {
        val destination = ByteArray(endIndex - startIndex - 32)
        decryptIntoByteArray(source, destination, 0, startIndex, endIndex)
        return destination
    }

    override fun decryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ) {
        val key = ByteArray(32)
        sharedSecret.copyInto(key, destinationOffset = 0, startIndex = 0, endIndex = 16)
        source.copyInto(key, destinationOffset = 16, startIndex = startIndex + 16, endIndex = startIndex + 32)

        val iv = ByteArray(16)
        source.copyInto(iv, destinationOffset = 0, startIndex = startIndex + 0, endIndex = startIndex + 4)
        sharedSecret.copyInto(iv, destinationOffset = 4, startIndex = 20, endIndex = 32)

        val cipher = AesCtr(key, iv)
        cipher.use {
            cipher.processBytes(
                source,
                destination,
                destinationOffset = destinationOffset,
                startIndex = startIndex + 32,
                endIndex
            )
        }
    }
}

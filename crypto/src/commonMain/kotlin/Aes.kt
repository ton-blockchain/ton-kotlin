package org.ton.kotlin.crypto

import io.github.andreypfau.kotlinx.crypto.AES
import io.github.andreypfau.kotlinx.crypto.CTRBlockCipher
import io.github.andreypfau.kotlinx.crypto.Sha256

class EncryptorAes(
    private val sharedSecret: ByteArray
) : Encryptor {
    override fun encryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ) {
        Sha256().update(source, startIndex, endIndex).digest(destination, destinationOffset)

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

        val cipher = CTRBlockCipher(AES(key), iv)
        cipher.processBytes(source, destination, destinationOffset = destinationOffset + 32, startIndex, endIndex)
    }
}

class DecryptorAes(
    private val sharedSecret: ByteArray
) : Decryptor {
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

        val cipher = CTRBlockCipher(AES(key), iv)
        cipher.processBytes(
            source,
            destination,
            destinationOffset = destinationOffset,
            startIndex = startIndex + 32,
            endIndex
        )
    }
}

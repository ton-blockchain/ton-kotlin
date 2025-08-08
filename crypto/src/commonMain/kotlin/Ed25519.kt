package org.ton.kotlin.crypto

import io.github.andreypfau.curve25519.ed25519.Ed25519
import io.github.andreypfau.curve25519.ed25519.Ed25519PublicKey

class DecryptorEd25519(
    private val privateKey: PrivateKeyEd25519,
) : Decryptor {
    override fun decryptToByteArray(source: ByteArray, startIndex: Int, endIndex: Int): ByteArray {
        val destination = ByteArray(endIndex - startIndex - 64)
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
        val publicKey = PublicKeyEd25519(source.copyOfRange(startIndex, startIndex + 32))
        val sharedKey = privateKey.computeSharedSecret(publicKey)
        val aes = DecryptorAes(sharedKey)
        aes.decryptIntoByteArray(
            source,
            destination,
            destinationOffset,
            startIndex + 32,
            endIndex
        )
    }

    override fun signIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ) {
        Ed25519.keyFromSeed(privateKey.key).sign(
            source.copyOfRange(startIndex, endIndex),
            destination,
            destinationOffset
        )
    }
}

class EncryptorEd25519(
    private val publicKey: PublicKeyEd25519,
) : Encryptor {
    override fun encryptToByteArray(source: ByteArray, startIndex: Int, endIndex: Int): ByteArray {
        val destination = ByteArray(endIndex - startIndex + 64)
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
        val pk = PrivateKeyEd25519.random()
        val sharedKey = pk.computeSharedSecret(publicKey)
        val aes = EncryptorAes(sharedKey)
        aes.encryptIntoByteArray(
            source,
            destination,
            destinationOffset + 32,
            startIndex,
            endIndex
        )
        pk.publicKey().key.copyInto(
            destination,
            destinationOffset,
            startIndex = 0,
            endIndex = 32
        )
    }

    override fun checkSignature(source: ByteArray, signature: ByteArray, startIndex: Int, endIndex: Int): Boolean {
        val pub = Ed25519PublicKey(publicKey.key)
        return pub.verify(source.copyOfRange(startIndex, endIndex), signature)
    }
}

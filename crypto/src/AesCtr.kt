package org.ton.kotlin.crypto

import io.github.andreypfau.kotlinx.crypto.sha2.sha256

@Deprecated("Use kotlinx-crypto instead")
public expect class AesCtr(key: ByteArray, iv: ByteArray) {
    public fun update(input: ByteArray, output: ByteArray = ByteArray(input.size)): ByteArray
    public fun doFinal(output: ByteArray): ByteArray
    public fun doFinal(): ByteArray
}

public class EncryptorAes(
    private val secret: ByteArray
) : Encryptor {
    override fun encrypt(data: ByteArray): ByteArray {
        val digest = sha256(data)

        val key = ByteArray(32)
        secret.copyInto(key, destinationOffset = 0, startIndex = 0, endIndex = 16)
        digest.copyInto(key, destinationOffset = 16, startIndex = 16, endIndex = 32)

        val iv = ByteArray(16)
        digest.copyInto(iv, destinationOffset = 0, startIndex = 0, endIndex = 4)
        secret.copyInto(iv, destinationOffset = 4, startIndex = 20, endIndex = 32)

        val cipher = AesCtr(key, iv)
        val encryptedData = cipher.update(data)

        return digest + encryptedData
    }

    override fun verify(message: ByteArray, signature: ByteArray?): Boolean =
        throw IllegalStateException("Can't verify by AES encryptor")
}

public class DecryptorAes(
    private val secret: ByteArray
) : Decryptor {
    override fun decrypt(data: ByteArray): ByteArray {
        check(data.size >= 32) { "data is too short: ${data.size}" }

        val digest = data.copyOfRange(0, 32)
        val key = ByteArray(32)
        secret.copyInto(key, destinationOffset = 0, startIndex = 0, endIndex = 16)
        digest.copyInto(key, destinationOffset = 16, startIndex = 16, endIndex = 32)

        val iv = ByteArray(16)
        digest.copyInto(iv, destinationOffset = 0, startIndex = 0, endIndex = 4)
        secret.copyInto(iv, destinationOffset = 4, startIndex = 20, endIndex = 32)

        val cipher = AesCtr(key, iv)
        val decryptedData = cipher.update(data.copyOfRange(32, data.size))

        val actualDigest = sha256(decryptedData)
        check(digest.contentEquals(actualDigest)) { "sha256 mismatch after decryption" }

        return decryptedData
    }

    override fun sign(message: ByteArray): ByteArray =
        throw IllegalStateException("Can't sign by AES decryptor")
}

package org.ton.kotlin.crypto

import io.github.andreypfau.kotlinx.crypto.Sha256
import kotlinx.io.bytestring.ByteString

sealed interface PublicKey {
    fun computeShortId(): ByteString
    fun createEncryptor(): Encryptor
}

class PublicKeyEd25519(
    internal val key: ByteArray
) : PublicKey {
    override fun computeShortId(): ByteString {
        val sha256 = Sha256()
        sha256.update(byteArrayOf(0x48, 0x13, 0xb4.toByte(), 0xc6.toByte()))
        sha256.update(key)
        return ByteString(*sha256.digest())
    }

    override fun createEncryptor() = EncryptorEd25519(this)
}

class PublicKeyAes(
    private val key: ByteArray
) : PublicKey {
    override fun computeShortId(): ByteString {
        val sha256 = Sha256()
        sha256.update(byteArrayOf(0x2d, 0xbc.toByte(), 0xad.toByte(), 0xd4.toByte()))
        sha256.update(key)
        return ByteString(*sha256.digest())
    }

    override fun createEncryptor() = EncryptorAes(key)
}

package org.ton.kotlin.crypto

import io.github.andreypfau.curve25519.ed25519.Ed25519
import io.github.andreypfau.curve25519.ed25519.Ed25519PrivateKey
import io.github.andreypfau.curve25519.ed25519.Ed25519PublicKey
import kotlin.jvm.JvmStatic
import kotlin.random.Random

public object Ed25519 {
    public const val KEY_SIZE_BYTES: Int = 32

    @JvmStatic
    public fun privateKey(random: Random = SecureRandom): ByteArray =
        Ed25519.generateKey(random).seed()

    @JvmStatic
    public fun publicKey(privateKey: ByteArray): ByteArray =
        Ed25519.keyFromSeed(privateKey).publicKey().toByteArray()

    @JvmStatic
    public fun sign(privateKey: ByteArray, message: ByteArray): ByteArray =
        Ed25519.keyFromSeed(privateKey).sign(message)

    @JvmStatic
    public fun verify(publicKey: ByteArray, message: ByteArray, signature: ByteArray): Boolean =
        Ed25519PublicKey(publicKey).verify(message, signature)

    @JvmStatic
    public fun sharedKey(privateKey: ByteArray, publicKey: ByteArray): ByteArray =
        Ed25519.keyFromSeed(privateKey).sharedKey(Ed25519PublicKey(publicKey))
}

public class DecryptorEd25519(
    private val privateKey: Ed25519PrivateKey,
    private val publicKey: Ed25519PublicKey? = null
) : Decryptor {
    public constructor(data: ByteArray) : this(Ed25519.keyFromSeed(data))

    override fun decrypt(data: ByteArray): ByteArray {
        return if (publicKey == null) {
            val publicKey = Ed25519PublicKey(data)
            val sharedKey = privateKey.sharedKey(publicKey)
            val aes = DecryptorAes(sharedKey)
            aes.decrypt(data.copyOfRange(32, data.size))
        } else {
            val sharedKey = privateKey.sharedKey(publicKey)
            val aes = DecryptorAes(sharedKey)
            aes.decrypt(data)
        }
    }

    override fun sign(message: ByteArray): ByteArray =
        privateKey.sign(message)
}

public class EncryptorEd25519(
    private val publicKey: Ed25519PublicKey,
    private val privateKey: Ed25519PrivateKey? = null
) : Encryptor {
    public constructor(data: ByteArray) : this(Ed25519PublicKey(data))

    override fun encrypt(data: ByteArray): ByteArray {
        val privateKey = privateKey ?: Ed25519.generateKey(SecureRandom)
        val sharedKey = privateKey.sharedKey(publicKey)
        val aes = EncryptorAes(sharedKey)
        val encrypted = aes.encrypt(data)
        return privateKey.publicKey().toByteArray() + encrypted
    }

    override fun verify(message: ByteArray, signature: ByteArray?): Boolean {
        if (signature == null || signature.isEmpty()) return false
        return publicKey.verify(message, signature)
    }
}

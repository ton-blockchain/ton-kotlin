package org.ton.kotlin.crypto

import io.github.andreypfau.curve25519.ed25519.Ed25519
import io.github.andreypfau.curve25519.edwards.CompressedEdwardsY
import io.github.andreypfau.curve25519.edwards.EdwardsPoint
import io.github.andreypfau.curve25519.montgomery.MontgomeryPoint
import io.github.andreypfau.curve25519.x25519.X25519
import io.github.andreypfau.kotlinx.crypto.sha512
import kotlinx.io.bytestring.ByteString
import kotlin.random.Random

sealed interface PrivateKey {
    fun computeShortId(): ByteString = publicKey().computeShortId()
    fun createDecryptor(): Decryptor
    fun publicKey(): PublicKey
}

class PrivateKeyEd25519(
    internal val key: ByteArray
) : PrivateKey {
    fun computeSharedSecret(publicKey: PublicKeyEd25519): ByteArray {
        val xPrivateKey = sha512(key).copyOf(32)
        val xPublicKey = MontgomeryPoint.from(
            EdwardsPoint.from(
                CompressedEdwardsY(
                    publicKey.key
                )
            )
        ).data
        val result = ByteArray(32)
        X25519.x25519(xPrivateKey, xPublicKey, result)
        return result
    }

    override fun createDecryptor() = DecryptorEd25519(this)

    override fun publicKey(): PublicKeyEd25519 {
        return PublicKeyEd25519(Ed25519.keyFromSeed(key).publicKey().toByteArray())
    }

    companion object {
        // TODO: secure random generation
        fun random(random: Random = Random): PrivateKeyEd25519 {
            val key = random.nextBytes(32)
            return PrivateKeyEd25519(key)
        }
    }
}

class PrivateKeyAes(
    private val key: ByteArray
) : PrivateKey {
    override fun createDecryptor() = DecryptorAes(key)

    override fun publicKey() = PublicKeyAes(key.copyOfRange(0, 32))
}

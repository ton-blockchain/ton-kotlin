package org.ton.kotlin.crypto

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import org.ton.kotlin.crypto.curve25519.constants.tables.ED25519_BASEPOINT_TABLE
import org.ton.kotlin.crypto.curve25519.edwards.CompressedEdwardsY
import org.ton.kotlin.crypto.curve25519.edwards.EdwardsPoint
import org.ton.kotlin.crypto.curve25519.internal.varTimeDoubleScalarBaseMul
import org.ton.kotlin.crypto.curve25519.montgomery.MontgomeryPoint
import org.ton.kotlin.crypto.curve25519.scalar.Scalar
import kotlin.random.Random

public class PrivateKeyEd25519(
    public val key: ByteString
) : PrivateKey {
    private val publicKey: PublicKeyEd25519 by lazy {
        val scalar = Scalar.fromByteArray(clampedScalar())
        val edwardsPoint = EdwardsPoint.mul(ED25519_BASEPOINT_TABLE, scalar)
        val compressedEdwardsY = CompressedEdwardsY(edwardsPoint)
        PublicKeyEd25519(ByteString(*compressedEdwardsY.data))
    }

    public fun computeSharedSecret(publicKey: PublicKeyEd25519): ByteArray {
        val s = Scalar.fromByteArray(clampedScalar())
        val montP = MontgomeryPoint(
            EdwardsPoint.from(
                CompressedEdwardsY(
                    publicKey.key.toByteArray()
                )
            )
        )
        montP.mul(montP, s)
        return montP.data
    }

    override fun createDecryptor(): Decryptor = DecryptorEd25519(this)

    override fun publicKey(): PublicKeyEd25519 = publicKey

    internal fun clampedScalar(): ByteArray {
        val digest = sha512(key.toByteArray())
        digest[0] = (digest[0].toInt() and 248).toByte()
        digest[31] = (digest[31].toInt() and 127).toByte()
        digest[31] = (digest[31].toInt() or 64).toByte()
        return digest
    }

    public companion object {
        public fun random(random: Random = SecureRandom): PrivateKeyEd25519 {
            val key = random.nextBytes(32)
            return PrivateKeyEd25519(ByteString(*key))
        }
    }
}

public class PublicKeyEd25519(
    public val key: ByteString
) : PublicKey {
    override fun createEncryptor(): EncryptorEd25519 = EncryptorEd25519(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PublicKeyEd25519) return false
        return key == other.key
    }

    override fun hashCode(): Int = key.hashCode()

    override fun toString(): String = key.toHexString()
}

public class DecryptorEd25519(
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
        val publicKey = PublicKeyEd25519(ByteString(*source.copyOfRange(startIndex, startIndex + 32)))
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
        val extsk = privateKey.clampedScalar()

        val hashR = Sha512().use {
            it.update(extsk, 32, extsk.size)
            it.update(source, startIndex, endIndex)
            it.digest()
        }
        val r = Scalar.fromWideByteArray(hashR)

        // R = rB
        val R = EdwardsPoint().mulBasepoint(ED25519_BASEPOINT_TABLE, r)
        val rCompressed = CompressedEdwardsY(R)

        // S = H(R,A,m)
        val s = Scalar()
        val hashRam = Sha512().use {
            it.update(rCompressed.data)
            it.update(privateKey.publicKey().key.toByteArray())
            it.update(source, startIndex, endIndex)
            it.digest()
        }
        s.setWideByteArray(hashRam)

        val a = Scalar.fromByteArray(extsk)
        s.mul(s, a)

        // S = (r + H(R,A,m)a)
        s.add(s, r)

        // S = (r + H(R,A,m)a) mod L
        rCompressed.data.copyInto(destination, destinationOffset)
        s.toByteArray(destination, destinationOffset + 32)
    }
}

public class EncryptorEd25519(
    private val publicKey: PublicKeyEd25519,
) : Encryptor {
    override fun encryptToByteArray(source: ByteArray, startIndex: Int, endIndex: Int): ByteArray {
        val destination = ByteArray(endIndex - startIndex + 64)
        encryptIntoByteArray(source, destination, startIndex = startIndex, endIndex = endIndex)
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
        val publicKeyBytes = publicKey.key.toByteArray()
        val aCompressed = CompressedEdwardsY(publicKeyBytes)
        val a = EdwardsPoint.from(aCompressed)

        // hram = H(R,A,m)
        val hash = Sha512().use {
            it.update(signature, endIndex = 32)
            it.update(publicKeyBytes)
            it.update(source, startIndex, endIndex)
            it.digest()
        }
        val k = Scalar.fromWideByteArray(hash)
        val s = Scalar.fromByteArray(signature, 32)

        // A = -A (Since we want SB - H(R,A,m)A)
        a.negate(a)

        // Check that [8]R == [8](SB - H(R,A,m)A)), by computing
        // [delta S]B - [delta A]H(R,A,m) - [delta]R, multiplying the
        // result by the cofactor, and checking if the result is
        // small order.
        //
        // Note: IsSmallOrder includes a cofactor multiply.
        val r = varTimeDoubleScalarBaseMul(k, a, s)
        val rCompressed = CompressedEdwardsY(r)

        return rCompressed.data.contentEquals(signature.copyOf(32))
    }
}

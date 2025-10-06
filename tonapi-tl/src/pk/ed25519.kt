package org.ton.api.pk

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.pub.PublicKeyEd25519
import org.ton.kotlin.crypto.Decryptor
import org.ton.kotlin.crypto.SecureRandom
import org.ton.kotlin.crypto.Signer
import org.ton.kotlin.tl.ByteStringBase64Serializer
import org.ton.kotlin.tl.TlConstructor
import org.ton.kotlin.tl.TlReader
import org.ton.kotlin.tl.TlWriter
import kotlin.jvm.JvmStatic
import kotlin.random.Random

public inline fun PrivateKeyEd25519(random: Random = SecureRandom): PrivateKeyEd25519 =
    PrivateKeyEd25519.generate(random)

@Serializable
@SerialName("pk.ed25519")
public data class PrivateKeyEd25519(
    @Serializable(ByteStringBase64Serializer::class)
    public val key: ByteString
) : PrivateKey, Decryptor, Signer {
    public constructor(key: ByteArray) : this(ByteString(*key.copyOf(32)))

    init {
        require(key.size == 32) { "key must be 32 byte long" }
    }

    private val _publicKey: PublicKeyEd25519 by lazy(LazyThreadSafetyMode.PUBLICATION) {
        PublicKeyEd25519(_privateKey.publicKey.key)
    }
    private val _privateKey: org.ton.kotlin.crypto.PrivateKeyEd25519 by lazy(LazyThreadSafetyMode.PUBLICATION) {
        org.ton.kotlin.crypto.PrivateKeyEd25519(key)
    }

    override fun publicKey(): PublicKeyEd25519 = _publicKey

    public fun sharedKey(publicKey: PublicKeyEd25519): ByteArray =
        _privateKey.computeSharedSecret(org.ton.kotlin.crypto.PublicKeyEd25519(publicKey.key))

    override fun toString(): String = "PrivateKeyEd25519(${key.toHexString()})"

    override fun decryptToByteArray(source: ByteArray, startIndex: Int, endIndex: Int): ByteArray {
        return _privateKey.decryptToByteArray(source, startIndex, endIndex)
    }

    override fun decryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ) {
        _privateKey.decryptIntoByteArray(source, destination, destinationOffset, startIndex, endIndex)
    }

    override fun signToByteArray(source: ByteArray, startIndex: Int, endIndex: Int): ByteArray {
        return _privateKey.signToByteArray(source, startIndex, endIndex)
    }

    override fun signIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ) {
        _privateKey.signIntoByteArray(source, destination, destinationOffset, startIndex, endIndex)
    }

    public companion object : TlConstructor<PrivateKeyEd25519>(
        schema = "pk.ed25519 key:int256 = PrivateKey"
    ) {
        @JvmStatic
        public fun tlConstructor(): TlConstructor<PrivateKeyEd25519> = this

        @JvmStatic
        public fun generate(random: Random = SecureRandom): PrivateKeyEd25519 =
            PrivateKeyEd25519(ByteString(*random.nextBytes(32)))

        @JvmStatic
        public fun of(byteArray: ByteArray): PrivateKeyEd25519 =
            when (byteArray.size) {
                32 -> PrivateKeyEd25519(byteArray)
                32 + Int.SIZE_BYTES -> decodeBoxed(byteArray)
                else -> throw IllegalArgumentException("Invalid key size: ${byteArray.size}")
            }

        public override fun encode(writer: TlWriter, value: PrivateKeyEd25519) {
            writer.writeRaw(value.key)
        }

        public override fun decode(reader: TlReader): PrivateKeyEd25519 {
            val key = reader.readByteString(32)
            return PrivateKeyEd25519(key)
        }
    }
}

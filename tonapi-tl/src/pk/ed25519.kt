package org.ton.kotlin.api.pk

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.api.pub.PublicKeyEd25519
import org.ton.kotlin.crypto.Decryptor
import org.ton.kotlin.crypto.DecryptorEd25519
import org.ton.kotlin.crypto.Ed25519
import org.ton.kotlin.crypto.SecureRandom
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
) : PrivateKey, Decryptor {
    public constructor(key: ByteArray) : this(ByteString(*key.copyOf(32)))

    init {
        require(key.size == 32) { "key must be 32 byte long" }
    }

    private val _publicKey: PublicKeyEd25519 by lazy(LazyThreadSafetyMode.PUBLICATION) {
        PublicKeyEd25519(ByteString(*Ed25519.publicKey(key.toByteArray())))
    }
    private val _decryptor by lazy(LazyThreadSafetyMode.PUBLICATION) {
        DecryptorEd25519(key.toByteArray())
    }

    override fun publicKey(): PublicKeyEd25519 = _publicKey

    public fun sharedKey(publicKey: PublicKeyEd25519): ByteArray =
        Ed25519.sharedKey(key.toByteArray(), publicKey.key.toByteArray())

    override fun toString(): String = "PrivateKeyEd25519(${key.toHexString()})"

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
                Ed25519.KEY_SIZE_BYTES -> PrivateKeyEd25519(byteArray)
                Ed25519.KEY_SIZE_BYTES + Int.SIZE_BYTES -> decodeBoxed(byteArray)
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

    override fun decrypt(data: ByteArray): ByteArray =
        _decryptor.decrypt(data)

    override fun sign(message: ByteArray): ByteArray =
        _decryptor.sign(message)
}

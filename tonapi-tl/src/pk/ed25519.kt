package org.ton.api.pk

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.pub.PublicKeyEd25519
import org.ton.crypto.Decryptor
import org.ton.crypto.DecryptorEd25519
import org.ton.crypto.Ed25519
import org.ton.crypto.SecureRandom
import org.ton.tl.*
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
    public constructor(key: ByteArray) : this(ByteString(key))

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

    public object TL : TlConstructor<PrivateKeyEd25519>(
        schema = "pk.ed25519 key:int256 = PrivateKey"
    ) {
        public override fun encode(writer: TlWriter, value: PrivateKeyEd25519) {
            writer.writeRaw(value.key)
        }

        public override fun decode(reader: TlReader): PrivateKeyEd25519 {
            val key = reader.readByteString(32)
            return PrivateKeyEd25519(key)
        }
    }

    public companion object : TlCodec<PrivateKeyEd25519> {
        @JvmStatic
        public fun tlConstructor(): TlConstructor<PrivateKeyEd25519> = TL

        @JvmStatic
        public fun generate(random: Random = SecureRandom): PrivateKeyEd25519 =
            PrivateKeyEd25519(ByteString(*random.nextBytes(32)))

        @JvmStatic
        public fun of(byteArray: ByteArray): PrivateKeyEd25519 =
            when (byteArray.size) {
                Ed25519.KEY_SIZE_BYTES -> PrivateKeyEd25519(byteArray)
                Ed25519.KEY_SIZE_BYTES + Int.SIZE_BYTES -> TL.decodeBoxed(byteArray)
                else -> throw IllegalArgumentException("Invalid key size: ${byteArray.size}")
            }

        override fun decode(reader: TlReader): PrivateKeyEd25519 = TL.decode(reader)

        override fun decodeBoxed(reader: TlReader): PrivateKeyEd25519 = TL.decodeBoxed(reader)

        override fun encode(writer: TlWriter, value: PrivateKeyEd25519): Unit = TL.encode(writer, value)

        override fun encodeBoxed(writer: TlWriter, value: PrivateKeyEd25519): Unit = TL.encodeBoxed(writer, value)
    }

    override fun decrypt(data: ByteArray): ByteArray =
        _decryptor.decrypt(data)

    override fun sign(message: ByteArray): ByteArray =
        _decryptor.sign(message)
}

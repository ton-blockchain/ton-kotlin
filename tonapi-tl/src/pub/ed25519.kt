@file:Suppress("NOTHING_TO_INLINE")

package org.ton.api.pub

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.adnl.AdnlIdShort
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.crypto.Ed25519
import org.ton.crypto.Encryptor
import org.ton.crypto.EncryptorEd25519
import org.ton.tl.ByteStringBase64Serializer
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter
import kotlin.jvm.JvmStatic

public inline fun PublicKeyEd25519(privateKey: PrivateKeyEd25519): PublicKeyEd25519 = PublicKeyEd25519.of(privateKey)

@Serializable
@SerialName("pub.ed25519")
@Polymorphic
public data class PublicKeyEd25519(
    @Serializable(ByteStringBase64Serializer::class)
    val key: ByteString
) : PublicKey, Encryptor {
    public constructor(byteArray: ByteArray) : this(ByteString(byteArray))

    private val _adnlIdShort: AdnlIdShort by lazy(LazyThreadSafetyMode.PUBLICATION) {
        AdnlIdShort(ByteString(*tlConstructor().hash(this)))
    }
    private val _encryptor by lazy(LazyThreadSafetyMode.PUBLICATION) {
        EncryptorEd25519(key.toByteArray())
    }

    override fun toAdnlIdShort(): AdnlIdShort = _adnlIdShort

    public object TL : TlConstructor<PublicKeyEd25519>(
        schema = "pub.ed25519 key:int256 = PublicKey",
    ) {
        override fun encode(writer: TlWriter, value: PublicKeyEd25519) {
            writer.writeRaw(value.key)
        }

        override fun decode(reader: TlReader): PublicKeyEd25519 {
            val key = reader.readByteString(32)
            return PublicKeyEd25519(key)
        }
    }

    public companion object {
        @JvmStatic
        public fun tlConstructor(): TlConstructor<PublicKeyEd25519> = TL

        @JvmStatic
        public fun of(privateKey: PrivateKeyEd25519): PublicKeyEd25519 =
            PublicKeyEd25519(ByteString(*Ed25519.publicKey(privateKey.key.toByteArray())))
    }

    override fun encrypt(data: ByteArray): ByteArray =
        _encryptor.encrypt(data)

    override fun verify(message: ByteArray, signature: ByteArray?): Boolean =
        _encryptor.verify(message, signature)
}

@file:Suppress("NOTHING_TO_INLINE")

package org.ton.api.pub

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.adnl.AdnlIdShort
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.kotlin.crypto.Encryptor
import org.ton.kotlin.crypto.EncryptorEd25519
import org.ton.kotlin.tl.ByteStringBase64Serializer
import org.ton.kotlin.tl.TlConstructor
import org.ton.kotlin.tl.TlReader
import org.ton.kotlin.tl.TlWriter
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
        AdnlIdShort(ByteString(*hash(this)))
    }
    private val _encryptor by lazy(LazyThreadSafetyMode.PUBLICATION) {
        EncryptorEd25519(org.ton.kotlin.crypto.PublicKeyEd25519(key))
    }

    override fun toAdnlIdShort(): AdnlIdShort = _adnlIdShort

    override fun toString(): String = "PublicKeyEd25519(${key.toHexString()})"

    public companion object : TlConstructor<PublicKeyEd25519>(
        schema = "pub.ed25519 key:int256 = PublicKey",
    ) {
        @JvmStatic
        public fun of(privateKey: PrivateKeyEd25519): PublicKeyEd25519 =
            privateKey.publicKey()

        override fun encode(writer: TlWriter, value: PublicKeyEd25519) {
            writer.writeRaw(value.key)
        }

        override fun decode(reader: TlReader): PublicKeyEd25519 {
            val key = reader.readByteString(32)
            return PublicKeyEd25519(key)
        }
    }

    override fun encryptIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ) {
        _encryptor.encryptIntoByteArray(source, destination, destinationOffset, startIndex, endIndex)
    }

    override fun checkSignature(
        source: ByteArray,
        signature: ByteArray,
        startIndex: Int,
        endIndex: Int
    ): Boolean {
        return _encryptor.checkSignature(source, signature, startIndex, endIndex)
    }
}

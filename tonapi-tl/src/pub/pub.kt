@file:Suppress("OPT_IN_USAGE")

package org.ton.api.pub

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.isEmpty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.api.adnl.AdnlIdShort
import org.ton.api.dht.DhtKeyDescription
import org.ton.api.dht.DhtUpdateRule
import org.ton.kotlin.crypto.Encryptor
import org.ton.kotlin.crypto.EncryptorAes
import org.ton.kotlin.crypto.EncryptorFail
import org.ton.kotlin.crypto.EncryptorNone
import org.ton.kotlin.tl.*

@Serializable
@JsonClassDiscriminator("@type")
public sealed interface PublicKey : Encryptor, TlObject<PublicKey> {
    override fun tlCodec(): TlCodec<out PublicKey> = Companion

    public fun toAdnlIdShort(): AdnlIdShort

    public companion object : TlCombinator<PublicKey>(
        PublicKey::class,
        PublicKeyEd25519::class to PublicKeyEd25519,
        PublicKeyUnencrypted::class to PublicKeyUnencrypted,
        PublicKeyAes::class to PublicKeyAes,
        PublicKeyOverlay::class to PublicKeyOverlay,
    )
}

@SerialName("pub.unenc")
@Serializable
public data class PublicKeyUnencrypted(
    @Serializable(ByteStringBase64Serializer::class)
    val data: ByteString
) : PublicKey, Encryptor by EncryptorNone {

    override fun toAdnlIdShort(): AdnlIdShort = AdnlIdShort(ByteString(*PublicKeyUnencrypted.hash(this)))

    public companion object : TlConstructor<PublicKeyUnencrypted>(
        schema = "pub.unenc data:bytes = PublicKey"
    ) {
        override fun encode(writer: TlWriter, value: PublicKeyUnencrypted) {
            writer.writeBytes(value.data)
        }

        override fun decode(reader: TlReader): PublicKeyUnencrypted {
            val data = reader.readByteString()
            return PublicKeyUnencrypted(data)
        }
    }
}

@SerialName("pub.aes")
@Serializable
public data class PublicKeyAes(
    @Serializable(ByteStringBase64Serializer::class)
    val key: ByteString
) : PublicKey, Encryptor by EncryptorAes(key.toByteArray()) {
    private val _adnlIdShort by lazy(LazyThreadSafetyMode.PUBLICATION) {
        AdnlIdShort(ByteString(*hash(this)))
    }

    override fun toAdnlIdShort(): AdnlIdShort = _adnlIdShort

    public companion object : TlConstructor<PublicKeyAes>(
        schema = "pub.aes key:int256 = PublicKey"
    ) {
        override fun encode(writer: TlWriter, value: PublicKeyAes) {
            writer.writeRaw(value.key)
        }

        override fun decode(reader: TlReader): PublicKeyAes {
            val key = reader.readByteString(32)
            return PublicKeyAes(key)
        }
    }
}

@SerialName("pub.overlay")
@Serializable
public data class PublicKeyOverlay(
    @Serializable(ByteStringBase64Serializer::class)
    val name: ByteString
) : PublicKey, Encryptor by EncryptorFail {

    override fun toAdnlIdShort(): AdnlIdShort = AdnlIdShort(
        ByteString(*PublicKeyOverlay.hash(this))
    )

    override fun checkSignature(source: ByteArray, signature: ByteArray, startIndex: Int, endIndex: Int): Boolean {
        val result = try {
            DhtKeyDescription.decodeBoxed(source)
        } catch (e: Exception) {
            return false
        }
        if (result.updateRule != DhtUpdateRule.OVERLAY_NODES) return false
        return result.signature.isEmpty()
    }

    public companion object : TlConstructor<PublicKeyOverlay>(
        schema = "pub.overlay name:bytes = PublicKey"
    ) {
        override fun encode(writer: TlWriter, value: PublicKeyOverlay) {
            writer.writeBytes(value.name)
        }

        override fun decode(reader: TlReader): PublicKeyOverlay {
            val name = reader.readByteString()
            return PublicKeyOverlay(name)
        }
    }
}

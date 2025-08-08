package org.ton.kotlin.crypto

import io.github.andreypfau.kotlinx.crypto.sha256
import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.TL
import org.ton.kotlin.tl.TlConstructorId
import org.ton.kotlin.tl.TlFixedSize
import org.ton.kotlin.tl.serializers.ByteArrayBase64Serializer
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer
import kotlin.io.encoding.Base64

@Serializable
sealed interface PublicKey {
    fun computeShortId(): ByteString {
        val value = TL.Boxed.encodeToByteArray(serializer(), this)
        return ByteString(*sha256(value))
    }

    fun createEncryptor(): Encryptor
}

@Serializable
@SerialName("pub.ed25519")
@TlConstructorId(0x4813b4c6)
class PublicKeyEd25519(
    @TlFixedSize(32)
    @Serializable(with = ByteArrayBase64Serializer::class)
    internal val key: ByteArray
) : PublicKey {
    override fun createEncryptor() = EncryptorEd25519(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PublicKeyEd25519) return false
        if (!key.contentEquals(other.key)) return false
        return true
    }

    override fun hashCode(): Int = key.contentHashCode()

    override fun toString(): String = Base64.encode(key)
}

@Serializable
@SerialName("pub.aes")
@TlConstructorId(0x2dbcadd4)
class PublicKeyAes(
    @TlFixedSize(32)
    private val key: ByteArray
) : PublicKey {
    override fun createEncryptor() = EncryptorAes(key)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PublicKeyEd25519) return false
        if (!key.contentEquals(other.key)) return false
        return true
    }

    override fun hashCode(): Int = key.contentHashCode()

    override fun toString(): String {
        return "PublicKeyAes(key=${key.toHexString()})"
    }
}

@Serializable
@SerialName("pub.overlay")
@TlConstructorId(0x34ba45cb)
data class PublicKeyOverlay(
    @Serializable(ByteStringBase64Serializer::class)
    val name: ByteString
) : PublicKey {
    override fun createEncryptor(): Encryptor {
        TODO("Not yet implemented")
    }
}

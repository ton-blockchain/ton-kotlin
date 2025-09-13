@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.kotlin.dht

import io.github.andreypfau.kotlinx.crypto.sha256
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.isEmpty
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.kotlin.adnl.AdnlAddressList
import org.ton.kotlin.adnl.AdnlIdFull
import org.ton.kotlin.adnl.AdnlIdShort
import org.ton.kotlin.adnl.AdnlNode
import org.ton.kotlin.crypto.PrivateKey
import org.ton.kotlin.crypto.PublicKey
import org.ton.kotlin.dht.bucket.Key
import org.ton.kotlin.tl.*
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
@SerialName("dht.node")
@TlConstructorId(0x84533248)
data class DhtNode(
    val id: AdnlIdFull,
    val addrList: AdnlAddressList = AdnlAddressList(),
    val version: Int = -1,
    val signature: ByteString = ByteString()
) {
    constructor(id: PublicKey, addressList: AdnlAddressList, signature: ByteString = ByteString()) : this(
        id = AdnlIdFull(id),
        addrList = addressList,
        version = -1,
        signature = signature
    )

    fun sign(key: PrivateKey): ByteString {
        val toSign = if (signature.isEmpty()) this else copy(signature = ByteString())
        val raw = TL.Boxed.encodeToByteArray(toSign)
        return ByteString(*key.createDecryptor().signToByteArray(raw))
    }

    fun signed(key: PrivateKey): DhtNode {
        val signature = sign(key)
        return copy(signature = signature)
    }

    fun isValid(): Boolean {
        if (signature.isEmpty()) return false
        val raw = TL.Boxed.encodeToByteArray(this.copy(signature = ByteString()))
        return id.publicKey.createEncryptor().checkSignature(raw, signature.toByteArray())
    }

    fun toAdnlNode(): AdnlNode = AdnlNode(id, addrList)
}

@Serializable
@TlConstructorId(0x7974a0be)
data class DhtNodes(
    val nodes: List<DhtNode>
) : List<DhtNode> by nodes

@Serializable
@SerialName("dht.key")
@TlConstructorId(0xf667de8f)
data class DhtKey(
    @Bits256
    val id: ByteString,
    val name: String,
    val idx: Int = 0,
) : Key {
    val keyId: DhtKeyId by lazy {
        DhtKeyId(ByteString(*sha256(TL.Boxed.encodeToByteArray(this))))
    }

    override val hash: ByteString get() = keyId.hash
}

@Serializable
sealed interface DhtUpdateRule {
    @Serializable
    @TlConstructorId(0xcc9f31f7)
    object Signature : DhtUpdateRule

    @Serializable
    @TlConstructorId(0x61578e14)
    object Anybody : DhtUpdateRule

    @Serializable
    @TlConstructorId(0x26779383)
    object OverlayNodes : DhtUpdateRule
}

@Serializable
@SerialName("dht.keyDescription")
@TlConstructorId(0x281d4e05)
data class DhtKeyDescription(
    val key: DhtKey,
    val id: PublicKey,
    val updateRule: DhtUpdateRule = DhtUpdateRule.Signature,
    val signature: ByteString = ByteString()
) : Key {
    override val hash: ByteString get() = key.hash

    fun toSign(): DhtKeyDescription = if (signature.isEmpty()) this else copy(signature = ByteString())

    fun signed(key: PrivateKey): DhtKeyDescription {
        val rawToSign = TL.Boxed.encodeToByteArray(toSign())
        val signature = ByteString(*key.createDecryptor().signToByteArray(rawToSign))
        return copy(signature = signature)
    }
}

@Serializable
@SerialName("dht.value")
@TlConstructorId(0x90ad27cb)
data class DhtValue(
    val key: DhtKeyDescription,
    val value: ByteString,
    val ttl: Int,
    val signature: ByteString = ByteString()
) {
    @OptIn(ExperimentalTime::class)
    fun isExpired(now: Instant = Clock.System.now()): Boolean {
        return (Instant.fromEpochSeconds(ttl.toLong()) - now).inWholeSeconds <= 0
    }

    fun signed(key: PrivateKey): DhtValue {
        val toSign = if (signature.isEmpty()) this else copy(signature = ByteString())
        val raw = TL.Boxed.encodeToByteArray(toSign)
        val signature = ByteString(*key.createDecryptor().signToByteArray(raw))
        return copy(signature = signature)
    }
}

@Serializable
@TlConstructorId(0x5a8aef81)
data class DhtPong(
    val randomId: Long
)

@Serializable
@TlConstructorId(0x7026fb08)
data object DhtStored

@Serializable
sealed interface DhtValueResult {
    fun valueOrNull(): DhtValue? {
        return when (this) {
            is Found -> value
            is NotFound -> null
        }
    }

    fun nodesOrNull(): DhtNodes? {
        return when (this) {
            is Found -> null
            is NotFound -> nodes
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @KeepGeneratedSerializer
    @Serializable(with = Found.Serializer::class)
    @SerialName("dht.valueFound")
    @TlConstructorId(0xe40cf774)
    data class Found(
        val value: DhtValue
    ) : DhtValueResult {
        internal object Serializer : KSerializer<Found> {
            override val descriptor: SerialDescriptor =
                SerialDescriptor($$"dht.valueFound$boxed", generatedSerializer().descriptor)

            override fun serialize(
                encoder: Encoder,
                value: Found
            ) {
                if (encoder is TlEncoder) {
                    encoder.encodeInt(0x90ad27cb.toInt()) // boxed constructor id, due 'dht.Value' polymorphic type, not dht.value
                }
                generatedSerializer().serialize(encoder, value)
            }

            override fun deserialize(decoder: Decoder): Found {
                if (decoder is TlDecoder) {
                    decoder.decodeInt() // boxed constructor id, due 'dht.Value' polymorphic type, not dht.value
                }
                return generatedSerializer().deserialize(decoder)
            }
        }
    }

    @Serializable
    @SerialName("dht.valueNotFound")
    @TlConstructorId(0xa2620568)
    data class NotFound(
        val nodes: DhtNodes
    ) : DhtValueResult
}

@Serializable
sealed interface DhtReversePingResult {
    @Serializable
    @TlConstructorId(0x2d1c7e6f)
    data class ClientNotFound(
        val nodes: DhtNodes
    ) : DhtReversePingResult

    @Serializable
    @TlConstructorId(0x204030a2)
    data object Ok : DhtReversePingResult
}

@Serializable
sealed interface DhtFunction<A> {
    val answerSerializer: KSerializer<A>

    @Serializable
    @TlConstructorId(0xcbeb3f18)
    data class Ping(
        val randomId: Long
    ) : DhtFunction<DhtPong> {
        override val answerSerializer: KSerializer<DhtPong> get() = DhtPong.serializer()
    }

    @Serializable
    @TlConstructorId(0x34934212)
    data class Store(
        val value: DhtValue
    ) : DhtFunction<DhtStored> {
        override val answerSerializer: KSerializer<DhtStored> get() = DhtStored.serializer()
    }

    @Serializable
    @TlConstructorId(0x6ce2ce6b)
    data class FindNode(
        @Bits256
        val key: ByteString,
        val k: Int
    ) : DhtFunction<DhtNodes> {
        override val answerSerializer: KSerializer<DhtNodes> get() = DhtNodes.serializer()
    }

    @Serializable
    @TlConstructorId(0xae4b6011)
    data class FindValue(
        @Bits256
        val key: ByteString,
        val k: Int,
    ) : DhtFunction<DhtValueResult> {
        override val answerSerializer: KSerializer<DhtValueResult> get() = DhtValueResult.serializer()
    }

    @Serializable
    @TlConstructorId(0xa97948ed)
    data object GetSignedAddressList : DhtFunction<DhtNode> {
        override val answerSerializer: KSerializer<DhtNode> get() = DhtNode.serializer()
    }

    @Serializable
    @TlConstructorId(0x222cbc61)
    data class RegisterReverseConnection(
        val node: AdnlIdFull,
        val ttl: Int,
        val signature: ByteString
    ) : DhtFunction<DhtStored> {
        override val answerSerializer: KSerializer<DhtStored> get() = DhtStored.serializer()
    }

    @Serializable
    @TlConstructorId(0x0b94a40a)
    data class RequestReversePing(
        val target: AdnlNode,
        val signature: ByteString,
        val client: AdnlIdShort,
        val k: Int
    ) : DhtFunction<DhtReversePingResult> {
        override val answerSerializer: KSerializer<DhtReversePingResult> get() = DhtReversePingResult.serializer()
    }

    @Serializable
    @TlConstructorId(0x7d530769)
    data class Query<T>(
        val node: DhtNode,
        val query: DhtFunction<T>
    ) : DhtFunction<T> {
        override val answerSerializer: KSerializer<T> get() = query.answerSerializer
    }
}

interface DhtService {
    suspend fun <T> query(query: DhtFunction<T>, serializer: KSerializer<DhtFunction<T>>): T

    suspend fun ping(randomId: Long): DhtPong
    suspend fun store(value: DhtValue): DhtStored
    suspend fun findNode(key: ByteString, k: Int): DhtNodes
    suspend fun findValue(key: ByteString, k: Int): DhtValueResult
    suspend fun getSignedAddressList(): DhtNode
    suspend fun registerReverseConnection(node: AdnlIdFull, ttl: Int, signature: ByteString): DhtStored
    suspend fun requestReversePing(
        target: AdnlNode,
        signature: ByteString,
        client: AdnlIdShort,
        k: Int
    ): DhtReversePingResult
}

@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.lite.client.internal

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.crypto.PublicKey
import org.ton.kotlin.tl.Bits256
import org.ton.kotlin.tl.TlConstructorId
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
internal sealed interface LiteMessage {
    @Serializable
    @SerialName("adnl.message.query")
    @TlConstructorId(0xb48bf97a)
    class Query(
        @Bits256
        val queryId: ByteString,
        val query: ByteString
    ) : LiteMessage

    @Serializable
    @SerialName("adnl.message.answer")
    @TlConstructorId(0x0fac8416)
    class Answer(
        @Bits256
        val queryId: ByteString,
        val query: ByteString
    ) : LiteMessage

    @Serializable
    @SerialName("tcp.ping")
    @TlConstructorId(0x4d082b9a)
    class Ping(
        val randomId: Long
    ) : LiteMessage

    @Serializable
    @SerialName("tcp.pong")
    @TlConstructorId(0xdc69fb03)
    class Pong(
        val randomId: Long
    ) : LiteMessage

    @Serializable
    @SerialName("tcp.authentificate")
    @TlConstructorId(0x445bab12)
    class Authentificate(val nonce: ByteString) : LiteMessage

    @Serializable
    @SerialName("tcp.authentificationNonce")
    @TlConstructorId(0xe35d4ab6)
    class AuthentificationNonce(val nonce: ByteString) : LiteMessage

    @Serializable
    @SerialName("tcp.authentificationComplete")
    @TlConstructorId(0xf7ad9ea6)
    class AuthentificationComplete(val key: PublicKey, val signature: ByteString) : LiteMessage
}

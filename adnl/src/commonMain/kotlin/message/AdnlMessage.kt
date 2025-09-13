@file:UseSerializers(ByteStringSerializer::class)

package org.ton.kotlin.adnl.message

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.adnl.serializers.ByteStringSerializer
import org.ton.kotlin.crypto.PublicKeyEd25519
import org.ton.kotlin.tl.Bits256
import org.ton.kotlin.tl.TlConstructorId

@Serializable
sealed interface AdnlMessage {
    val size: Int

    @TlConstructorId(0xe673c3bb)
    @Serializable
    data class CreateChannel(
        val key: PublicKeyEd25519,
        val date: Int
    ) : AdnlMessage {
        override val size: Int get() = 40
    }

    @TlConstructorId(0x60dd1d69)
    @Serializable
    data class ConfirmChannel(
        val key: PublicKeyEd25519,
        val peerKey: PublicKeyEd25519,
        val date: Int
    ) : AdnlMessage {
        override val size: Int get() = 72
    }

    @TlConstructorId(0x204818f5)
    @Serializable
    data class Custom(
        val data: ByteString
    ) : AdnlMessage {
        override val size: Int get() = data.size + 12
    }

    @TlConstructorId(0x17f8dfda)
    @Serializable
    data object Nop : AdnlMessage {
        override val size: Int get() = 4
    }

    @TlConstructorId(0x10c20520)
    @Serializable
    data class Reinit(
        val date: Int,
    ) : AdnlMessage {
        override val size: Int get() = 8
    }

    @TlConstructorId(0xb48bf97a)
    @Serializable
    data class Query(
        @Bits256
        val queryId: ByteString,
        val query: ByteString
    ) : AdnlMessage {
        override val size: Int get() = query.size + 44
    }

    @TlConstructorId(0x0fac8416)
    @Serializable
    data class Answer(
        @Bits256
        val queryId: ByteString,
        val answer: ByteString
    ) : AdnlMessage {
        override val size: Int get() = answer.size + 44
    }

    @TlConstructorId(0xfd452d39)
    @Serializable
    data class Part(
        @Bits256
        val hash: ByteString,
        val totalSize: Int,
        val offset: Int,
        val data: ByteString
    ) : AdnlMessage {
        override val size: Int get() = data.size + 48

        override fun toString(): String {
            return "Part(hash=${hash.toHexString()}, totalSize=$totalSize, offset=$offset, data=$data)"
        }
    }
}

class AdnlMessageList(
    val messages: List<AdnlMessage>
) {
    constructor(vararg messages: AdnlMessage) : this(messages.toList())

    fun singleOrNull(): AdnlMessage? {
        return if (messages.size == 1) messages[0] else null
    }

    fun multipleOrNull(): List<AdnlMessage>? {
        return if (messages.size > 1) messages else null
    }
}

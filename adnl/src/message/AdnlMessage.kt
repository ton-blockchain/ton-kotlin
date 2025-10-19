@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.kotlin.adnl.message

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.crypto.PublicKeyEd25519
import org.ton.kotlin.tl.Bits256
import org.ton.kotlin.tl.TlConstructorId
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
public sealed interface AdnlMessage {
    public val size: Int

    @TlConstructorId(0xe673c3bb)
    @Serializable
    public class CreateChannel(
        public val key: PublicKeyEd25519,
        public val date: Int
    ) : AdnlMessage {
        override val size: Int get() = 40
    }

    @TlConstructorId(0x60dd1d69)
    @Serializable
    public class ConfirmChannel(
        public val key: PublicKeyEd25519,
        public val peerKey: PublicKeyEd25519,
        public val date: Int
    ) : AdnlMessage {
        override val size: Int get() = 72
    }

    @TlConstructorId(0x204818f5)
    @Serializable
    public class Custom(
        public val data: ByteString
    ) : AdnlMessage {
        override val size: Int get() = data.size + 12
    }

    @TlConstructorId(0x17f8dfda)
    @Serializable
    public object Nop : AdnlMessage {
        override val size: Int get() = 4
    }

    @TlConstructorId(0x10c20520)
    @Serializable
    public class Reinit(
        public val date: Int,
    ) : AdnlMessage {
        override val size: Int get() = 8
    }

    @TlConstructorId(0xb48bf97a)
    @Serializable
    public class Query(
        @Bits256
        public val queryId: ByteString,
        public val query: ByteString
    ) : AdnlMessage {
        override val size: Int get() = query.size + 44
    }

    @TlConstructorId(0x0fac8416)
    @Serializable
    public class Answer(
        @Bits256
        public val queryId: ByteString,
        public val answer: ByteString
    ) : AdnlMessage {
        override val size: Int get() = answer.size + 44
    }

    @TlConstructorId(0xfd452d39)
    @Serializable
    public class Part(
        @Bits256
        public val hash: ByteString,
        public val totalSize: Int,
        public val offset: Int,
        public val data: ByteString
    ) : AdnlMessage {
        override val size: Int get() = data.size + 48
    }
}

public class AdnlMessageList(
    public val messages: List<AdnlMessage>
) {
    public constructor(vararg messages: AdnlMessage) : this(messages.toList())

    public fun singleOrNull(): AdnlMessage? {
        return if (messages.size == 1) messages[0] else null
    }

    public fun multipleOrNull(): List<AdnlMessage>? {
        return if (messages.size > 1) messages else null
    }
}

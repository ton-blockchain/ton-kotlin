package org.ton.kotlin.adnl.message

import kotlinx.io.Buffer
import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.crypto.PublicKeyEd25519
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
sealed interface AdnlMessage {
    val size: Int

    data class CreateChannel(
        val key: PublicKeyEd25519,
        val date: Instant
    ) : AdnlMessage {
        override val size: Int get() = 40
    }

    data class ConfirmChannel(
        val key: PublicKeyEd25519,
        val peerKey: PublicKeyEd25519,
        val date: Instant
    ) : AdnlMessage {
        override val size: Int get() = 72
    }

    data class Custom(
        val data: Buffer
    ) : AdnlMessage {
        override val size: Int get() = data.size.toInt() + 12
    }

    data object Nop : AdnlMessage {
        override val size: Int get() = 4
    }

    data class Reinit(
        val date: Instant,
    ) : AdnlMessage {
        override val size: Int get() = 8
    }

    data class Answer(
        val queryId: ByteString,
        val answer: Buffer
    ) : AdnlMessage {
        override val size: Int get() = answer.size.toInt() + 44
    }

    data class Part(
        val hash: ByteString,
        val totalSize: Int,
        val offset: Int,
        val data: Buffer
    ) : AdnlMessage {
        override val size: Int get() = data.size.toInt() + 48
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

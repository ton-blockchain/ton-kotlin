@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.kotlin.rldp

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.fec.FecType
import org.ton.kotlin.tl.Bits256
import org.ton.kotlin.tl.TlConstructorId
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
sealed interface RldpMessage {
    @Serializable
    @SerialName("rldp.message")
    @TlConstructorId(0x7d1bcd1e)
    data class Custom(
        @Bits256
        val id: ByteString,
        val data: ByteString,
    ) : RldpMessage

    @Serializable
    @SerialName("rldp.query")
    @TlConstructorId(0x8a794d69)
    data class Query(
        @Bits256
        val queryId: ByteString,
        val maxAnswerSize: Long,
        val timeout: Int,
        val data: ByteString,
    ) : RldpMessage

    @Serializable
    @SerialName("rldp.answer")
    @TlConstructorId(0xa3fc5c03)
    data class Answer(
        @Bits256
        val queryId: ByteString,
        val data: ByteString,
    ) : RldpMessage
}

@Serializable
sealed interface Rldp2MessagePart {
    val transferId: ByteString
    val part: Int

    sealed interface Acknowledgment : Rldp2MessagePart

    @Serializable
    @SerialName("rldp2.messagePart")
    @TlConstructorId(0x11480b6e)
    data class Part(
        @Bits256
        override val transferId: ByteString,
        val fecType: FecType,
        override val part: Int,
        val totalSize: Long,
        val seqno: Int,
        val data: ByteString,
    ) : Rldp2MessagePart

    @Serializable
    @SerialName("rldp2.confirm")
    @TlConstructorId(0x23e69945)
    data class Confirm(
        @Bits256
        override val transferId: ByteString,
        override val part: Int,
        val maxSeqno: Int,
        val receivedMask: Int,
        val receivedCount: Int,
    ) : Acknowledgment

    @Serializable
    @SerialName("rldp2.complete")
    @TlConstructorId(0x36b9081f)
    data class Complete(
        @Bits256
        override val transferId: ByteString,
        override val part: Int,
    ) : Acknowledgment
}

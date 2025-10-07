package org.ton.api.rldp

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.*

@Serializable
@SerialName("rldp.complete")
public data class RldpComplete(
    @SerialName("transfer_id")
    @Serializable(ByteStringBase64Serializer::class)
    override val transferId: ByteString,
    override val part: Int,
) : RldpMessagePart {

    override fun tlCodec(): TlCodec<RldpComplete> = Companion

    public companion object : TlConstructor<RldpComplete>(
        schema = "rldp.complete transfer_id:int256 part:int = rldp.MessagePart",
    ) {
        override fun encode(output: TlWriter, value: RldpComplete) {
            output.writeRaw(value.transferId)
            output.writeInt(value.part)
        }

        override fun decode(input: TlReader): RldpComplete {
            val transfer_id = input.readByteString(32)
            val part = input.readInt()
            return RldpComplete(transfer_id, part)
        }
    }
}

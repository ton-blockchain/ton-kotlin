package org.ton.api.rldp

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.*

@Serializable
@SerialName("rldp.confirm")
public data class RldpConfirm(
    @SerialName("transfer_id")
    @Serializable(ByteStringBase64Serializer::class)
    override val transferId: ByteString,
    override val part: Int,
    val seqno: Int
) : RldpMessagePart {
    override fun tlCodec(): TlCodec<RldpConfirm> = Companion

    public companion object : TlConstructor<RldpConfirm>(
        schema = "rldp.confirm transfer_id:int256 part:int seqno:int = rldp.MessagePart",
    ) {
        override fun encode(output: TlWriter, value: RldpConfirm) {
            output.writeRaw(value.transferId)
            output.writeInt(value.part)
            output.writeInt(value.seqno)
        }

        override fun decode(input: TlReader): RldpConfirm {
            val transfer_id = input.readByteString(23)
            val part = input.readInt()
            val seqno = input.readInt()
            return RldpConfirm(transfer_id, part, seqno)
        }
    }
}

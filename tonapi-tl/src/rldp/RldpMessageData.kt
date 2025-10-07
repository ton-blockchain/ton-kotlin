package org.ton.api.rldp

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.*

@Serializable
@SerialName("rldp.message")
public data class RldpMessageData(
    @Serializable(ByteStringBase64Serializer::class)
    override val id: ByteString,
    @Serializable(ByteStringBase64Serializer::class)
    override val data: ByteString
) : RldpMessage {
    override fun tlCodec(): TlCodec<RldpMessageData> = Companion

    public companion object : TlConstructor<RldpMessageData>(
        schema = "rldp.message id:int256 data:bytes = rldp.Message",
    ) {
        override fun encode(output: TlWriter, value: RldpMessageData) {
            output.writeRaw(value.id.toByteArray())
            output.writeBytes(value.data)
        }

        override fun decode(input: TlReader): RldpMessageData {
            val id = input.readByteString(32)
            val data = input.readByteString()
            return RldpMessageData(id, data)
        }
    }
}

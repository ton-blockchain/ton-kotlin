package org.ton.api.rldp

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.fec.FecType
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("rldp.messagePart")
public data class RldpMessagePartData(
    @get:JvmName("transferId")
    @Serializable(ByteStringBase64Serializer::class)
    override val transferId: ByteString,

    @SerialName("fec_type")
    @get:JvmName("fecType")
    val fecType: FecType,

    @get:JvmName("part")
    override val part: Int,

    @SerialName("total_size")
    @get:JvmName("totalSize")
    val totalSize: Long,

    @get:JvmName("seqno")
    val seqno: Int,

    @get:JvmName("data")
    @Serializable(ByteStringBase64Serializer::class)
    val data: ByteString
) : RldpMessagePart {
    override fun tlCodec(): TlCodec<RldpMessagePartData> = Companion

    public companion object : TlConstructor<RldpMessagePartData>(
        schema = "rldp.messagePart transfer_id:int256 fec_type:fec.Type part:int total_size:long seqno:int data:bytes = rldp.MessagePart",
    ) {
        override fun encode(output: TlWriter, value: RldpMessagePartData) {
            output.writeRaw(value.transferId)
            output.write(FecType, value.fecType)
            output.writeInt(value.part)
            output.writeLong(value.totalSize)
            output.writeInt(value.seqno)
            output.writeBytes(value.data)
        }

        override fun decode(input: TlReader): RldpMessagePartData {
            val transfer_id = input.readByteString(32)
            val fec_type = input.read(FecType)
            val part = input.readInt()
            val total_size = input.readLong()
            val seqno = input.readInt()
            val data = input.readByteString()
            return RldpMessagePartData(transfer_id, fec_type, part, total_size, seqno, data)
        }
    }
}

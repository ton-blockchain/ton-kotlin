package org.ton.lite.api.liteserver

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.validatorStats")
public data class LiteServerValidatorStats(
    @get:JvmName("mode")
    val mode: Int,

    @get:JvmName("id")
    val id: TonNodeBlockIdExt,

    @get:JvmName("count")
    val count: Int,

    @get:JvmName("complete")
    val complete: Boolean,

    @SerialName("state_proof")
    @get:JvmName("stateProof")
    @Serializable(ByteStringBase64Serializer::class)
    val stateProof: ByteString,

    @SerialName("data_proof")
    @get:JvmName("dataProof")
    @Serializable(ByteStringBase64Serializer::class)
    val dataProof: ByteString
) {
    public companion object : TlCodec<LiteServerValidatorStats> by LiteServerValidatorStatsTlConstructor
}

private object LiteServerValidatorStatsTlConstructor : TlConstructor<LiteServerValidatorStats>(
    schema = "liteServer.validatorStats mode:# id:tonNode.blockIdExt count:int complete:Bool state_proof:bytes data_proof:bytes = liteServer.ValidatorStats"
) {
    override fun decode(reader: TlReader): LiteServerValidatorStats {
        val mode = reader.readInt()
        val id = reader.read(TonNodeBlockIdExt)
        val count = reader.readInt()
        val complete = reader.readBoolean()
        val stateProofBoc = reader.readByteString()
        val dataProofBoc = reader.readByteString()
        return LiteServerValidatorStats(mode, id, count, complete, stateProofBoc, dataProofBoc)
    }

    override fun encode(writer: TlWriter, value: LiteServerValidatorStats) {
        writer.writeInt(value.mode)
        writer.write(TonNodeBlockIdExt, value.id)
        writer.writeInt(value.count)
        writer.writeBoolean(value.complete)
        writer.writeBytes(value.stateProof)
        writer.writeBytes(value.dataProof)
    }
}

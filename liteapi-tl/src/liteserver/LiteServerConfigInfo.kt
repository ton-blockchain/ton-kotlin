package org.ton.lite.api.liteserver

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.configInfo")
public data class LiteServerConfigInfo(
    @get:JvmName("mode")
    val mode: Int,

    @get:JvmName("id")
    val id: TonNodeBlockIdExt,

    @get:JvmName("stateProof")
    @Serializable(ByteStringBase64Serializer::class)
    val stateProof: ByteString,

    @get:JvmName("configProof")
    @Serializable(ByteStringBase64Serializer::class)
    val configProof: ByteString
) {
    public companion object : TlCodec<LiteServerConfigInfo> by LiteServerConfigInfoTlConstructor
}

private object LiteServerConfigInfoTlConstructor : TlConstructor<LiteServerConfigInfo>(
    schema = "liteServer.configInfo mode:# id:tonNode.blockIdExt state_proof:bytes config_proof:bytes = liteServer.ConfigInfo"
) {
    override fun decode(reader: TlReader): LiteServerConfigInfo {
        val mode = reader.readInt()
        val id = reader.read(TonNodeBlockIdExt)
        val stateProof = reader.readByteString()
        val configProof = reader.readByteString()
        return LiteServerConfigInfo(mode, id, stateProof, configProof)
    }

    override fun encode(writer: TlWriter, value: LiteServerConfigInfo) {
        writer.writeInt(value.mode)
        writer.write(TonNodeBlockIdExt, value.id)
        writer.writeBytes(value.stateProof)
        writer.writeBytes(value.configProof)
    }
}

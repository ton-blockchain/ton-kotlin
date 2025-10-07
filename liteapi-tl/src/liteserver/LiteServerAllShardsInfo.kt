package org.ton.lite.api.liteserver

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.allShardsInfo")
public data class LiteServerAllShardsInfo(
    @get:JvmName("id")
    val id: TonNodeBlockIdExt,

    @get:JvmName("proof")
    @Serializable(ByteStringBase64Serializer::class)
    val proof: ByteString,

    @get:JvmName("data")
    @Serializable(ByteStringBase64Serializer::class)
    val data: ByteString
) {
    public companion object : TlCodec<LiteServerAllShardsInfo> by LiteServerAllShardsInfoTlConstructor
}

private object LiteServerAllShardsInfoTlConstructor : TlConstructor<LiteServerAllShardsInfo>(
    schema = "liteServer.allShardsInfo id:tonNode.blockIdExt proof:bytes data:bytes = liteServer.AllShardsInfo"
) {
    override fun decode(reader: TlReader): LiteServerAllShardsInfo {
        val id = reader.read(TonNodeBlockIdExt)
        val proof = reader.readByteString()
        val data = reader.readByteString()
        return LiteServerAllShardsInfo(id, proof, data)
    }

    override fun encode(writer: TlWriter, value: LiteServerAllShardsInfo) {
        writer.write(TonNodeBlockIdExt, value.id)
        writer.writeBytes(value.proof)
        writer.writeBytes(value.data)
    }
}

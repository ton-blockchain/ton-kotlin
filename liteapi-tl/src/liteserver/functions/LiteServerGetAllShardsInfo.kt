package org.ton.lite.api.liteserver.functions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.lite.api.liteserver.LiteServerAllShardsInfo
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.getAllShardsInfo")
public data class LiteServerGetAllShardsInfo(
    @get:JvmName("id")
    val id: TonNodeBlockIdExt
) : TLFunction<LiteServerGetAllShardsInfo, LiteServerAllShardsInfo> {
    override fun tlCodec(): TlCodec<LiteServerGetAllShardsInfo> = LiteServerGetAllShardsInfoTlConstructor
    override fun resultTlCodec(): TlCodec<LiteServerAllShardsInfo> = LiteServerAllShardsInfo

    public companion object : TlCodec<LiteServerGetAllShardsInfo> by LiteServerGetAllShardsInfoTlConstructor
}

private object LiteServerGetAllShardsInfoTlConstructor : TlConstructor<LiteServerGetAllShardsInfo>(
    schema = "liteServer.getAllShardsInfo id:tonNode.blockIdExt = liteServer.AllShardsInfo"
) {
    override fun decode(reader: TlReader): LiteServerGetAllShardsInfo {
        val id = reader.read(TonNodeBlockIdExt)
        return LiteServerGetAllShardsInfo(id)
    }

    override fun encode(writer: TlWriter, value: LiteServerGetAllShardsInfo) {
        writer.write(TonNodeBlockIdExt, value.id)
    }
}

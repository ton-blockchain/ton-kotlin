package org.ton.lite.api.liteserver.functions

import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.lite.api.liteserver.LiteServerConfigInfo
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
public data class LiteServerGetConfigParams(
    @get:JvmName("mode")
    val mode: Int,

    @get:JvmName("id")
    val id: TonNodeBlockIdExt,

    @get:JvmName("paramList")
    val paramList: List<Int>
) : TLFunction<LiteServerGetConfigParams, LiteServerConfigInfo> {
    override fun tlCodec(): TlCodec<LiteServerGetConfigParams> = LiteServerGetConfigParams
    override fun resultTlCodec(): TlCodec<LiteServerConfigInfo> = LiteServerConfigInfo

    public companion object : TlCodec<LiteServerGetConfigParams> by LiteServerGetConfigParamsTlConstructor
}

private object LiteServerGetConfigParamsTlConstructor : TlConstructor<LiteServerGetConfigParams>(
    schema = "liteServer.getConfigParams mode:# id:tonNode.blockIdExt param_list:(vector int) = liteServer.ConfigInfo"
) {
    override fun decode(reader: TlReader): LiteServerGetConfigParams {
        val mode = reader.readInt()
        val id = reader.read(TonNodeBlockIdExt)
        val paramList = reader.readVector {
            readInt()
        }
        return LiteServerGetConfigParams(mode, id, paramList)
    }

    override fun encode(writer: TlWriter, value: LiteServerGetConfigParams) {
        writer.writeInt(value.mode)
        writer.write(TonNodeBlockIdExt, value.id)
        writer.writeVector(value.paramList) {
            writeInt(it)
        }
    }
}

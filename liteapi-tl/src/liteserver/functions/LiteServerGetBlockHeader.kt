package org.ton.lite.api.liteserver.functions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.lite.api.liteserver.LiteServerBlockHeader
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.getBlockHeader")
public data class LiteServerGetBlockHeader(
    @get:JvmName("id")
    val id: TonNodeBlockIdExt,

    @get:JvmName("mode")
    val mode: Int
) : TLFunction<LiteServerGetBlockHeader, LiteServerBlockHeader> {
    override fun tlCodec(): TlCodec<LiteServerGetBlockHeader> = LiteServerGetBlockHeaderTlConstructor
    override fun resultTlCodec(): TlCodec<LiteServerBlockHeader> = LiteServerBlockHeader

    public companion object : TlCodec<LiteServerGetBlockHeader> by LiteServerGetBlockHeaderTlConstructor
}

private object LiteServerGetBlockHeaderTlConstructor : TlConstructor<LiteServerGetBlockHeader>(
    schema = "liteServer.getBlockHeader id:tonNode.blockIdExt mode:# = liteServer.BlockHeader"
) {
    override fun decode(reader: TlReader): LiteServerGetBlockHeader {
        val id = reader.read(TonNodeBlockIdExt)
        val mode = reader.readInt()
        return LiteServerGetBlockHeader(id, mode)
    }

    override fun encode(writer: TlWriter, value: LiteServerGetBlockHeader) {
        writer.write(TonNodeBlockIdExt, value.id)
        writer.writeInt(value.mode)
    }
}

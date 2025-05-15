package org.ton.kotlin.lite.api.liteserver.functions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.api.tonnode.TonNodeBlockIdExt
import org.ton.kotlin.lite.api.liteserver.LiteServerBlockData
import org.ton.kotlin.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.getBlock")
public data class LiteServerGetBlock(
    @get:JvmName("id")
    val id: TonNodeBlockIdExt,
) : TLFunction<LiteServerGetBlock, LiteServerBlockData> {
    override fun tlCodec(): TlCodec<LiteServerGetBlock> = LiteServerGetBlockTlConstructor

    override fun resultTlCodec(): TlCodec<LiteServerBlockData> = LiteServerBlockData

    public companion object : TlCodec<LiteServerGetBlock> by LiteServerGetBlockTlConstructor
}

private object LiteServerGetBlockTlConstructor : TlConstructor<LiteServerGetBlock>(
    schema = "liteServer.getBlock id:tonNode.blockIdExt = liteServer.BlockData"
) {
    override fun decode(reader: TlReader): LiteServerGetBlock {
        val id = reader.read(TonNodeBlockIdExt)
        return LiteServerGetBlock(id)
    }

    override fun encode(writer: TlWriter, value: LiteServerGetBlock) {
        writer.write(TonNodeBlockIdExt, value.id)
    }
}

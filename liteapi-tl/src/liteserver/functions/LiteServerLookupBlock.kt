package org.ton.lite.api.liteserver.functions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockId
import org.ton.lite.api.liteserver.LiteServerBlockHeader
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.lookupBlock")
public data class LiteServerLookupBlock(
    @get:JvmName("mode")
    val mode: Int,

    @get:JvmName("id")
    val id: TonNodeBlockId,

    @get:JvmName("lt")
    val lt: Long?,

    @get:JvmName("utime")
    val utime: Int?
) : TLFunction<LiteServerLookupBlock, LiteServerBlockHeader> {
    override fun tlCodec(): TlCodec<LiteServerLookupBlock> = LiteServerLookupBlockTlConstructor
    override fun resultTlCodec(): TlCodec<LiteServerBlockHeader> = LiteServerBlockHeader

    public companion object : TlCodec<LiteServerLookupBlock> by LiteServerLookupBlockTlConstructor {
        public const val ID_MASK: Int = 1
        public const val LT_MASK: Int = 2
        public const val UTIME_MASK: Int = 4
    }
}

private object LiteServerLookupBlockTlConstructor : TlConstructor<LiteServerLookupBlock>(
    schema = "liteServer.lookupBlock mode:# id:tonNode.blockId lt:mode.1?long utime:mode.2?int = liteServer.BlockHeader"
) {
    override fun decode(reader: TlReader): LiteServerLookupBlock {
        val mode = reader.readInt()
        val id = reader.read(TonNodeBlockId)
        val lt = reader.readNullable(mode, 1, TlReader::readLong)
        val utime = reader.readNullable(mode, 2, TlReader::readInt)
        return LiteServerLookupBlock(mode, id, lt, utime)
    }

    override fun encode(writer: TlWriter, value: LiteServerLookupBlock) {
        val mode = value.mode
        writer.writeInt(mode)
        writer.write(TonNodeBlockId, value.id)
        writer.writeNullable(mode, 1, value.lt, TlWriter::writeLong)
        writer.writeNullable(mode, 2, value.utime, TlWriter::writeInt)
    }
}

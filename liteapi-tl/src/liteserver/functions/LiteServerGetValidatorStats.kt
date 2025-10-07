package org.ton.lite.api.liteserver.functions

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.lite.api.liteserver.LiteServerValidatorStats
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.getValidatorStats")
public data class LiteServerGetValidatorStats(
    @get:JvmName("mode")
    val mode: Int,

    @get:JvmName("id")
    val id: TonNodeBlockIdExt,

    @get:JvmName("limit")
    val limit: Int,

    @get:JvmName("startAfter")
    @Serializable(ByteStringBase64Serializer::class)
    val startAfter: ByteString?,

    @get:JvmName("modifiedAfter")
    val modifiedAfter: Int?
) : TLFunction<LiteServerGetValidatorStats, LiteServerValidatorStats> {
    init {
        require(startAfter == null || startAfter.size == 32) { "startAfter must be 32 bytes long" }
    }

    public companion object : TlCodec<LiteServerGetValidatorStats> by LiteServerGetValidatorStatsTlConstructor

    override fun tlCodec(): TlCodec<LiteServerGetValidatorStats> = LiteServerGetValidatorStats

    override fun resultTlCodec(): TlCodec<LiteServerValidatorStats> = LiteServerValidatorStats
}

private object LiteServerGetValidatorStatsTlConstructor : TlConstructor<LiteServerGetValidatorStats>(
    schema = "liteServer.getValidatorStats#091a58bc mode:# id:tonNode.blockIdExt limit:int start_after:mode.0?int256 modified_after:mode.2?int = liteServer.ValidatorStats",
    id = 0x091a58bc
) {
    override fun decode(reader: TlReader): LiteServerGetValidatorStats {
        val mode = reader.readInt()
        val id = reader.read(TonNodeBlockIdExt)
        val limit = reader.readInt()
        val startAfter = reader.readNullable(mode, 0) {
            readByteString(32)
        }
        val modifiedAfter = reader.readNullable(mode, 2, TlReader::readInt)
        return LiteServerGetValidatorStats(mode, id, limit, startAfter, modifiedAfter)
    }

    override fun encode(writer: TlWriter, value: LiteServerGetValidatorStats) {
        writer.writeInt(value.mode)
        writer.write(TonNodeBlockIdExt, value.id)
        writer.writeInt(value.limit)
        writer.writeNullable(value.mode, 0, value.startAfter) {
            writeRaw(it)
        }
        writer.writeNullable(value.mode, 2, value.modifiedAfter, TlWriter::writeInt)
    }
}

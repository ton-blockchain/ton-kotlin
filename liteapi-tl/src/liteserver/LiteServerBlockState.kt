package org.ton.lite.api.liteserver

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.blockState")
public data class LiteServerBlockState(
    @get:JvmName("id")
    val id: TonNodeBlockIdExt,

    @SerialName("root_hash")
    @get:JvmName("rootHash")
    @Serializable(ByteStringBase64Serializer::class)
    val rootHash: ByteString,

    @SerialName("file_hash")
    @get:JvmName("fileHash")
    @Serializable(ByteStringBase64Serializer::class)
    val fileHash: ByteString,

    @get:JvmName("data")
    @Serializable(ByteStringBase64Serializer::class)
    val data: ByteString
) {
    init {
        require(rootHash.size == 32) { "rootHash must be 32 bytes long" }
        require(fileHash.size == 32) { "fileHash must be 32 bytes long" }
    }

    public companion object : TlCodec<LiteServerBlockState> by LiteServerBlockStateTlConstructor
}

private object LiteServerBlockStateTlConstructor : TlConstructor<LiteServerBlockState>(
    schema = "liteServer.blockState id:tonNode.blockIdExt root_hash:int256 file_hash:int256 data:bytes = liteServer.BlockState"
) {
    override fun decode(reader: TlReader): LiteServerBlockState {
        val id = reader.read(TonNodeBlockIdExt)
        val rootHash = reader.readByteString(32)
        val fileHash = reader.readByteString(32)
        val data = reader.readByteString()
        return LiteServerBlockState(id, rootHash, fileHash, data)
    }

    override fun encode(writer: TlWriter, value: LiteServerBlockState) {
        writer.write(TonNodeBlockIdExt, value.id)
        writer.writeRaw(value.rootHash)
        writer.writeRaw(value.fileHash)
        writer.writeBytes(value.data)
    }
}
